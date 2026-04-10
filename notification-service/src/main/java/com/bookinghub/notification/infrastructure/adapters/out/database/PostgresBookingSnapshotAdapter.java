package com.bookinghub.notification.infrastructure.adapters.out.database;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresBookingSnapshotAdapter implements BookingSnapshotRepository {

  private final JpaBookingSnapshotRepository jpa;

  @Override
  public void save(BookingSnapshot snapshot) {
    jpa.save(toEntity(snapshot));
  }

  @Override
  public Optional<BookingSnapshot> findByBookingId(UUID bookingId) {
    return jpa.findById(bookingId).map(this::toDomain);
  }

  @Override
  public List<BookingSnapshot> findByClientId(String clientId) {
    return jpa.findByClientId(clientId).stream().map(this::toDomain).toList();
  }

  @Override
  public List<BookingSnapshot> findByProfessionalId(UUID professionalId) {
    return jpa.findByProfessionalId(professionalId).stream().map(this::toDomain).toList();
  }

  @Override
  public List<BookingSnapshot> findConfirmedWithReminderPending(
      LocalDateTime from, LocalDateTime to) {
    return jpa.findConfirmedWithReminderPending(from, to).stream().map(this::toDomain).toList();
  }

  private BookingSnapshotEntity toEntity(BookingSnapshot s) {
    return BookingSnapshotEntity.builder()
        .bookingId(s.getBookingId())
        .clientId(s.getClientId())
        .professionalId(s.getProfessionalId())
        .startDatetime(s.getStartDatetime())
        .endDatetime(s.getEndDatetime())
        .status(s.getStatus())
        .updatedAt(s.getUpdatedAt())
        .clientEmail(s.getClientEmail())
        .professionalEmail(s.getProfessionalEmail())
        .reminderSent(s.isReminderSent())
        .build();
  }

  private BookingSnapshot toDomain(BookingSnapshotEntity e) {
    return BookingSnapshot.builder()
        .bookingId(e.getBookingId())
        .clientId(e.getClientId())
        .professionalId(e.getProfessionalId())
        .startDatetime(e.getStartDatetime())
        .endDatetime(e.getEndDatetime())
        .status(e.getStatus())
        .updatedAt(e.getUpdatedAt())
        .clientEmail(e.getClientEmail())
        .professionalEmail(e.getProfessionalEmail())
        .reminderSent(e.isReminderSent())
        .build();
  }
}
