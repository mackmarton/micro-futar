package org.bme.micro_futar.tracking.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bme.micro_futar.shared.enums.NotificationType;
import org.bme.micro_futar.tracking.enums.NotificationAudience;
import org.bme.micro_futar.tracking.enums.NotificationStatus;

import java.time.ZonedDateTime;

/**
 * Outbox row for one email to one addressee. {@code dedupKey} is the event's dedup key plus the audience, so a
 * redelivered event never produces a second email.
 */
@Data
@Entity
@NoArgsConstructor
@Table(indexes = @Index(name = "idx_notification_status_next_attempt", columnList = "status, nextAttemptAt"))
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String dedupKey;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationAudience audience;
    @Column(nullable = false)
    private String toEmail;
    private String toName;
    private Long shipmentId;
    private String parcelNumber;
    private String senderName;
    private String recipientName;
    private String recipientAddress;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;
    private int attempts;
    @Column(length = 2000)
    private String lastError;
    private ZonedDateTime createdAt;
    private ZonedDateTime nextAttemptAt;
    private ZonedDateTime sentAt;
}
