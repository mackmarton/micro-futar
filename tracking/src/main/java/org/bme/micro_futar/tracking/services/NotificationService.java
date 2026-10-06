package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.shared.dtos.ShipmentNotificationEventDTO;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.bme.micro_futar.tracking.enums.NotificationStatus;
import org.bme.micro_futar.tracking.repositories.NotificationRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Turns an event into one outbox row per addressee. Every row is saved in its own transaction, so a duplicate
     * (unique {@code dedupKey}) only skips that row.
     */
    public void enqueue(ShipmentNotificationEventDTO event) {
        if (event.getType() == NotificationType.SHIPMENT_CREATED) {
            enqueue(event, NotificationAudience.SENDER, event.getSenderEmail(), event.getSenderName());
        }
        enqueue(event, NotificationAudience.RECIPIENT, event.getRecipientEmail(), event.getRecipientName());
    }

    private void enqueue(ShipmentNotificationEventDTO event, NotificationAudience audience, String toEmail, String toName) {
        String dedupKey = event.getDedupKey() + ":" + audience;
        if (toEmail == null || toEmail.isBlank()) {
            log.warn("No email address for notification {}, skipping", dedupKey);
            return;
        }
        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.info("Notification {} already enqueued, skipping", dedupKey);
            return;
        }
        Notification notification = new Notification();
        notification.setDedupKey(dedupKey);
        notification.setType(event.getType());
        notification.setAudience(audience);
        notification.setToEmail(toEmail);
        notification.setToName(toName);
        notification.setShipmentId(event.getShipmentId());
        notification.setParcelNumber(event.getParcelNumber());
        notification.setSenderName(event.getSenderName());
        notification.setRecipientName(event.getRecipientName());
        notification.setRecipientAddress(event.getRecipientAddress());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(ZonedDateTime.now());
        notification.setNextAttemptAt(ZonedDateTime.now());
        try {
            notificationRepository.saveAndFlush(notification);
            log.info("Enqueued notification {}", dedupKey);
        } catch (DataIntegrityViolationException _) {
            log.info("Notification {} enqueued concurrently, skipping", dedupKey);
        }
    }
}
