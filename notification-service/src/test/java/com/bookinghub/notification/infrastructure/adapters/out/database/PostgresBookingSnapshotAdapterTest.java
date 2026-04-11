package com.bookinghub.notification.infrastructure.adapters.out.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresBookingSnapshotAdapterTest {

    @Mock
    private JpaBookingSnapshotRepository jpa;

    private PostgresBookingSnapshotAdapter adapter;

    private BookingSnapshotEntity sampleEntity;
    private UUID bookingId;

    @BeforeEach
    void setUp() {
        adapter = new PostgresBookingSnapshotAdapter(jpa);
        bookingId = UUID.randomUUID();
        sampleEntity = BookingSnapshotEntity.builder()
                .bookingId(bookingId)
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now())
                .endDatetime(LocalDateTime.now().plusHours(1))
                .status("CONFIRMED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();
    }

    @Test
    void save_shouldPersistEntity() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(bookingId).clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now()).endDatetime(LocalDateTime.now().plusHours(1))
                .status("CONFIRMED").updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com").professionalEmail("pro@example.com")
                .reminderSent(false).build();

        adapter.save(snapshot);

        verify(jpa).save(any(BookingSnapshotEntity.class));
    }

    @Test
    void findByBookingId_shouldReturnMappedDomain_whenFound() {
        when(jpa.findById(bookingId)).thenReturn(Optional.of(sampleEntity));

        Optional<BookingSnapshot> result = adapter.findByBookingId(bookingId);

        assertThat(result).isPresent();
        assertThat(result.get().getBookingId()).isEqualTo(bookingId);
        assertThat(result.get().getClientEmail()).isEqualTo("client@example.com");
    }

    @Test
    void findByBookingId_shouldReturnEmpty_whenNotFound() {
        when(jpa.findById(bookingId)).thenReturn(Optional.empty());

        Optional<BookingSnapshot> result = adapter.findByBookingId(bookingId);

        assertThat(result).isEmpty();
    }

    @Test
    void findByClientId_shouldReturnMappedList() {
        when(jpa.findByClientId("client-1")).thenReturn(List.of(sampleEntity));

        List<BookingSnapshot> result = adapter.findByClientId("client-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClientId()).isEqualTo("client-1");
    }

    @Test
    void findByProfessionalId_shouldReturnMappedList() {
        UUID profId = sampleEntity.getProfessionalId();
        when(jpa.findByProfessionalId(profId)).thenReturn(List.of(sampleEntity));

        List<BookingSnapshot> result = adapter.findByProfessionalId(profId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfessionalId()).isEqualTo(profId);
    }

    @Test
    void findConfirmedWithReminderPending_shouldReturnMappedList() {
        LocalDateTime from = LocalDateTime.now().plusHours(23);
        LocalDateTime to = LocalDateTime.now().plusHours(25);
        when(jpa.findConfirmedWithReminderPending(from, to)).thenReturn(List.of(sampleEntity));

        List<BookingSnapshot> result = adapter.findConfirmedWithReminderPending(from, to);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).isReminderSent()).isFalse();
    }
}
