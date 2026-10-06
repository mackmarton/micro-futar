package org.bme.micro_futar.tracking;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.bme.micro_futar.shared.dtos.ShipmentDTO;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.bme.micro_futar.tracking.enums.NotificationStatus;
import org.bme.micro_futar.tracking.repositories.NotificationRepository;
import org.bme.micro_futar.tracking.services.NotificationSender;
import org.bme.micro_futar.tracking.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.assertj.core.groups.Tuple;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = "notification.sender.poll-interval-ms=3600000")
@EnableTestContainers
class NotificationTests {

    @Autowired
    private NotificationService notificationService;
    @Autowired
    private NotificationSender notificationSender;
    @Autowired
    private NotificationRepository notificationRepository;
    @MockitoBean
    private JavaMailSender mailSender;

    private final ShipmentDTO shipment = ShipmentDTO.builder()
            .id(42L)
            .parcelNumber("abc-123")
            .senderName("Feladó Ferenc")
            .senderEmail("sender@example.com")
            .senderPhone("+36301234567")
            .senderLocationCountryId(1L)
            .senderZip("1011")
            .senderLocationCityId(1L)
            .senderAddress("Budapest, Kossuth tér 2.")
            .senderLatitude(47.5)
            .senderLongitude(19.0)
            .recipientName("Címzett Cecília")
            .recipientEmail("recipient@example.com")
            .recipientPhone("+36307654321")
            .recipientLocationCountryId(1L)
            .recipientZip("1011")
            .recipientLocationCityId(1L)
            .recipientAddress("Budapest, Fő utca 1.")
            .recipientLatitude(47.5)
            .recipientLongitude(19.0)
            .packageSizeId(1L)
            .build();

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        reset(mailSender);
        when(mailSender.createMimeMessage()).thenAnswer(_ -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    void shipmentCreatedNotifiesSenderAndRecipientOnce() {
        var event = ShipmentNotificationEventDTO.forShipment(NotificationType.SHIPMENT_CREATED, shipment);

        notificationService.enqueue(event);
        notificationService.enqueue(event);

        assertThat(notificationRepository.findAll())
                .extracting(Notification::getAudience, Notification::getToEmail)
                .containsExactlyInAnyOrder(
                        Tuple.tuple(NotificationAudience.SENDER, "sender@example.com"),
                        Tuple.tuple(NotificationAudience.RECIPIENT, "recipient@example.com"));
    }

    @Test
    void everyFailedDeliveryAttemptIsNotifiedButRedeliveriesAreNot() {
        var firstAttempt = ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, 1L, shipment);
        var secondAttempt = ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, 2L, shipment);

        notificationService.enqueue(firstAttempt);
        notificationService.enqueue(firstAttempt);
        notificationService.enqueue(secondAttempt);

        assertThat(notificationRepository.findAll())
                .extracting(Notification::getDedupKey)
                .containsExactlyInAnyOrder(
                        "DELIVERY_FAILED:assignment:1:RECIPIENT",
                        "DELIVERY_FAILED:assignment:2:RECIPIENT");
    }

    @Test
    void senderRendersAndSendsPendingNotifications() throws Exception {
        notificationService.enqueue(ShipmentNotificationEventDTO.forShipment(NotificationType.SHIPMENT_CREATED, shipment));

        notificationSender.sendDueNotifications();

        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(2)).send(captor.capture());
        MimeMessage recipientMail = captor.getAllValues().stream()
                .filter(m -> hasRecipient(m, "recipient@example.com"))
                .findFirst().orElseThrow();
        assertThat(recipientMail.getSubject()).isEqualTo("Csomagot adtak fel Önnek – abc-123");
        assertThat(recipientMail.getFrom()[0].toString()).contains("noreply@micro-futar.hu");
        String html = (String) recipientMail.getContent();
        assertThat(html)
                .contains("Kedves <span>Címzett Cecília</span>!")
                .contains("Feladó Ferenc")
                .contains("Budapest, Fő utca 1.")
                .contains("/portal/tracking?trackingNumber=abc-123");
        assertThat(notificationRepository.findAll())
                .allSatisfy(n -> assertThat(n.getStatus()).isEqualTo(NotificationStatus.SENT));
    }

    @Test
    void failedSendIsRetriedLater() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));
        notificationService.enqueue(ShipmentNotificationEventDTO.forShipment(NotificationType.DELIVERED, shipment));

        notificationSender.sendDueNotifications();
        notificationSender.sendDueNotifications();

        // The second run must not pick the row up again before its backoff has passed.
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        List<Notification> notifications = notificationRepository.findAll();
        assertThat(notifications).singleElement().satisfies(n -> {
            assertThat(n.getStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(n.getAttempts()).isEqualTo(1);
            assertThat(n.getLastError()).contains("SMTP down");
            assertThat(n.getNextAttemptAt()).isAfter(n.getCreatedAt());
        });
    }

    private static boolean hasRecipient(MimeMessage message, String email) {
        try {
            return message.getAllRecipients()[0].toString().equals(email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
