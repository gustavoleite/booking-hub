package com.bookinghub.notification.infrastructure.adapters.out.database;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaBookingSnapshotRepository
        extends JpaRepository<BookingSnapshotEntity, UUID> {

    List<BookingSnapshotEntity> findByClientId(String clientId);

    List<BookingSnapshotEntity> findByProfessionalId(UUID professionalId);

    @Query("SELECT s FROM BookingSnapshotEntity s WHERE s.status = 'CONFIRMED' "
            + "AND s.reminderSent = false "
            + "AND s.startDatetime >= :from AND s.startDatetime <= :to")
    List<BookingSnapshotEntity> findConfirmedWithReminderPending(
      @Param("from") LocalDateTime from,
      @Param("to") LocalDateTime to);
}
