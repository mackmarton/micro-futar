package org.bme.micro_futar.tracking.kafka;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.tracking.IntegrationTest;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.bme.micro_futar.tracking.enums.NotificationStatus;
import org.bme.micro_futar.tracking.repositories.NotificationRepository;
import org.bme.micro_futar.tracking.services.NotificationSender;
import org.bme.micro_futar.tracking.support.KafkaTestClient;
import org.bme.micro_futar.tracking.support.TestFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** The scheduler is effectively off in tests, so every test triggers the sending itself. */
@IntegrationTest
class NotificationMailingTests {

    @Autowired private KafkaTestClient kafka;
    @Autowired private NotificationSender sender;
    @Autowired private NotificationRepository notifications;
    @Autowired private JavaMailSender mailSender;

    @Value("${notification.sender.batch-size}")
    private int batchSize;
    @Value("${notification.sender.max-attempts}")
    private int maxAttempts;

    @BeforeEach
    void mailSenderCreatesRealMessages() {
        when(mailSender.createMimeMessage()).thenAnswer(_ -> new MimeMessage(Session.getInstance(new Properties())));
    }

    @Test
    void dueNotificationIsMailedAndMarkedSent() {
        givenNotification(NotificationType.DELIVERED);

        sender.sendDueNotifications();

        verify(mailSender).send(any(MimeMessage.class));
        assertThat(notifications.findAll()).singleElement().satisfies(notification -> {
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getAttempts()).isEqualTo(1);
            assertThat(notification.getSentAt()).isNotNull();
        });
    }

    @Test
    void recipientMailTellsWhoSentTheParcelAndWhereToTrackIt() throws Exception {
        givenNotification(NotificationType.SHIPMENT_CREATED);

        sender.sendDueNotifications();

        MimeMessage mail = mailTo(sentMails(2), TestFixtures.RECIPIENT_EMAIL);
        assertThat(mail.getFrom()[0].toString()).contains("noreply@micro-futar.hu");
        assertThat((String) mail.getContent())
                .contains("Kedves <span>Lukas Gruber</span>!")
                .contains("Kovács Eszter")
                .contains("Neubaugasse 25")
                .contains("/portal/tracking?trackingNumber=" + TestFixtures.PARCEL_NUMBER);
    }

    @ParameterizedTest
    @CsvSource({
            "SHIPMENT_CREATED, SENDER,    Sikeres csomagfeladás",
            "SHIPMENT_CREATED, RECIPIENT, Csomagot adtak fel Önnek",
            "OUT_FOR_DELIVERY, RECIPIENT, Csomagja úton van Önhöz",
            "DELIVERED,        RECIPIENT, Csomagja kézbesítve",
            "DELIVERY_FAILED,  RECIPIENT, Sikertelen kézbesítési kísérlet"
    })
    void mailSubjectMatchesTheEventAndTheAddressee(NotificationType type, NotificationAudience audience,
                                                   String heading) throws Exception {
        givenNotification(type);

        sender.sendDueNotifications();

        String email = audience == NotificationAudience.SENDER
                ? TestFixtures.SENDER_EMAIL
                : TestFixtures.RECIPIENT_EMAIL;
        MimeMessage mail = mailTo(sentMails(type == NotificationType.SHIPMENT_CREATED ? 2 : 1), email);
        assertThat(mail.getSubject()).isEqualTo(heading + " – " + TestFixtures.PARCEL_NUMBER);
        assertThat((String) mail.getContent()).contains(heading);
    }

    @Test
    void sentNotificationIsNotMailedAgain() {
        givenNotification(NotificationType.DELIVERED);
        sender.sendDueNotifications();

        sender.sendDueNotifications();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void allDueNotificationsAreSentInOneRunEvenBeyondTheBatchSize() {
        for (long assignmentId = 1; assignmentId <= batchSize + 1; assignmentId++) {
            publish(ShipmentNotificationEventDTO.forAssignment(NotificationType.DELIVERY_FAILED, assignmentId,
                    TestFixtures.shipment().build()));
        }

        sender.sendDueNotifications();

        assertThat(notifications.findAll())
                .hasSize(batchSize + 1)
                .allSatisfy(notification -> assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT));
    }

    @Test
    void failedSendStaysPendingAndRecordsTheError() {
        givenSmtpIsDown();
        givenNotification(NotificationType.DELIVERED);

        sender.sendDueNotifications();

        assertThat(notifications.findAll()).singleElement().satisfies(notification -> {
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(notification.getAttempts()).isEqualTo(1);
            assertThat(notification.getLastError()).contains("SMTP down");
            assertThat(notification.getSentAt()).isNull();
        });
    }

    @Test
    void failedNotificationIsNotRetriedBeforeItsBackoffPasses() {
        givenSmtpIsDown();
        givenNotification(NotificationType.DELIVERED);
        sender.sendDueNotifications();

        sender.sendDueNotifications();

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }

    @Test
    void retryDelayDoublesUpToAnHour() {
        givenSmtpIsDown();
        givenNotification(NotificationType.DELIVERED);
        List<Long> expectedMinutes = List.of(1L, 2L, 4L, 8L, 16L, 32L, 60L);

        for (long minutes : expectedMinutes) {
            makeDue();
            // the column keeps microseconds, hence the margins
            Instant before = Instant.now().truncatedTo(ChronoUnit.MILLIS);
            sender.sendDueNotifications();
            Instant after = Instant.now().plusMillis(1);

            Duration delay = Duration.ofMinutes(minutes);
            assertThat(onlyNotification().getNextAttemptAt().toInstant())
                    .isBetween(before.plus(delay), after.plus(delay));
        }
    }

    @Test
    void notificationIsGivenUpAfterTheLastAllowedAttempt() {
        givenSmtpIsDown();
        givenNotification(NotificationType.DELIVERED);
        for (int attempt = 1; attempt < maxAttempts; attempt++) {
            makeDue();
            sender.sendDueNotifications();
        }
        assertThat(onlyNotification().getStatus()).isEqualTo(NotificationStatus.PENDING);

        makeDue();
        sender.sendDueNotifications();
        makeDue();
        sender.sendDueNotifications();

        assertThat(onlyNotification().getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(onlyNotification().getAttempts()).isEqualTo(maxAttempts);
        verify(mailSender, times(maxAttempts)).send(any(MimeMessage.class));
    }

    @Test
    void successfulRetryMarksItSentAndClearsTheError() {
        givenSmtpIsDown();
        givenNotification(NotificationType.DELIVERED);
        sender.sendDueNotifications();
        doNothing().when(mailSender).send(any(MimeMessage.class));
        makeDue();

        sender.sendDueNotifications();

        assertThat(onlyNotification()).satisfies(notification -> {
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getAttempts()).isEqualTo(2);
            assertThat(notification.getLastError()).isNull();
        });
    }

    @Test
    void overlongErrorIsTruncatedToFitTheColumn() {
        doThrow(new MailSendException("554 Transaction failed. ".repeat(200)))
                .when(mailSender).send(any(MimeMessage.class));
        givenNotification(NotificationType.DELIVERED);

        sender.sendDueNotifications();

        assertThat(onlyNotification().getLastError()).hasSize(2000);
        assertThat(onlyNotification().getAttempts()).isEqualTo(1);
    }

    private void givenSmtpIsDown() {
        doThrow(new MailSendException("SMTP down")).when(mailSender).send(any(MimeMessage.class));
    }

    private void givenNotification(NotificationType type) {
        publish(ShipmentNotificationEventDTO.forShipment(type, TestFixtures.shipment().build()));
    }

    private void publish(ShipmentNotificationEventDTO event) {
        kafka.sendAndAwaitConsumption("shipment-notification-topic", event.getShipmentId().toString(), event);
    }

    /** Stands in for waiting out the backoff. */
    private void makeDue() {
        Notification notification = onlyNotification();
        notification.setNextAttemptAt(ZonedDateTime.now().minusSeconds(1));
        notifications.save(notification);
    }

    private Notification onlyNotification() {
        List<Notification> all = notifications.findAll();
        assertThat(all).hasSize(1);
        return all.getFirst();
    }

    private List<MimeMessage> sentMails(int count) {
        var captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(count)).send(captor.capture());
        return new ArrayList<>(captor.getAllValues());
    }

    private static MimeMessage mailTo(List<MimeMessage> mails, String email) throws MessagingException, IOException {
        for (MimeMessage mail : mails) {
            if (mail.getAllRecipients()[0].toString().equals(email)) {
                return mail;
            }
        }
        throw new AssertionError("No mail was sent to " + email);
    }
}
