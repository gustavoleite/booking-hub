package com.bookinghub.notification.infrastructure.adapters.out.database;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBookingSnapshotRepository
    extends JpaRepository<BookingSnapshotEntity, UUID> {

  List<BookingSnapshotEntity> findByClientId(String clientId);

  List<BookingSnapshotEntity> findByProfessionalId(UUID professionalId);
}
