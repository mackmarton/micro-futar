package org.bme.micro_futar.tracking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bme.micro_futar.tracking.entities.Notification;
import org.bme.micro_futar.tracking.enums.NotificationStatus;
import org.bme.micro_futar.tracking.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Drains the notification outbox. Failed sends are retried with exponential backoff (1, 2, 4, ... minutes, capped
 * at an hour) and marked {@code FAILED} after {@code max-attempts}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {

    private static final long MAX_BACKOFF_MINUTES = 60;

    private final NotificationRepository notificationRepository;
    private final NotificationMailService notificationMailService;
    private final TransactionTemplate transactionTemplate;

    @Value("${notification.sender.batch-size}")
    private int batchSize;
    @Value("${notification.sender.max-attempts}")
    private int maxAttempts;

    @Scheduled(fixedDelayString = "${notification.sender.poll-interval-ms}")
    public void sendDueNotifications() {
        Integer processed;
        do {
            processed = transactionTemplate.execute(_ -> sendBatch());
        } while (processed != null && processed == batchSize);
    }

    private int sendBatch() {
        List<Notification> batch = notificationRepository.lockDueBatch(ZonedDateTime.now(), batchSize);
        for (Notification notification : batch) {
            send(notification);
        }
        return batch.size();
    }

    private void send(Notification notification) {
        notification.setAttempts(notification.getAttempts() + 1);
        try {
            notificationMailService.send(notification);
            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(ZonedDateTime.now());
            notification.setLastError(null);
            log.info("Sent notification {} to {}", notification.getDedupKey(), notification.getToEmail());
        } catch (Exception e) {
            notification.setLastError(truncate(e.toString()));
            if (notification.getAttempts() >= maxAttempts) {
                notification.setStatus(NotificationStatus.FAILED);
                log.error("Giving up on notification {} after {} attempts", notification.getDedupKey(),
                        notification.getAttempts(), e);
            } else {
                long backoffMinutes = Math.min(1L << Math.min(notification.getAttempts() - 1, 6), MAX_BACKOFF_MINUTES);
                notification.setNextAttemptAt(ZonedDateTime.now().plusMinutes(backoffMinutes));
                log.warn("Failed to send notification {} (attempt {}), retrying in {} min",
                        notification.getDedupKey(), notification.getAttempts(), backoffMinutes, e);
            }
        }
        notificationRepository.save(notification);
    }

    private static String truncate(String value) {
        return value.length() <= 2000 ? value : value.substring(0, 2000);
    }
}
