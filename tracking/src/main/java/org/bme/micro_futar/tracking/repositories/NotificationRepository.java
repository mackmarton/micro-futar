package org.bme.micro_futar.tracking.repositories;

import org.bme.micro_futar.tracking.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    boolean existsByDedupKey(String dedupKey);

    /**
     * Locks a batch of due notifications for the current transaction. {@code SKIP LOCKED} lets every tracking
     * replica run the sender concurrently without two of them picking up the same row.
     */
    @Query(value = "SELECT * FROM notification WHERE status = 'PENDING' AND next_attempt_at <= :now " +
            "ORDER BY id LIMIT :limit FOR UPDATE SKIP LOCKED", nativeQuery = true)
    List<Notification> lockDueBatch(@Param("now") ZonedDateTime now, @Param("limit") int limit);
}
