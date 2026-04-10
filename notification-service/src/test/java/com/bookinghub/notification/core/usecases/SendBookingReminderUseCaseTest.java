package com.bookinghub.notification.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.EmailPort;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SendBookingReminderUseCaseTest {

    @Mock
    private BookingSnapshotRepository repository;

    @Mock
    private EmailPort emailPort;

    private SendBookingReminderUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendBookingReminderUseCase(repository, emailPort);
    }

    @Test
    void shouldSendReminderEmailsAndMarkSent() {
        BookingSnapshot snapshot = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusHours(24))
                .endDatetime(LocalDateTime.now().plusHours(25))
                .status("CONFIRMED")
                .updatedAt(LocalDateTime.now())
                .clientEmail("client@example.com")
                .professionalEmail("pro@example.com")
                .reminderSent(false)
                .build();

        when(repository.findConfirmedWithReminderPending(any(), any())).thenReturn(List.of(snapshot));

        useCase.execute();

        // Two emails: one for client, one for professional
        verify(emailPort).send(eq("client@example.com"), contains("Reminder"), anyString());
        verify(emailPort).send(eq("pro@example.com"), contains("Reminder"), anyString());

        // Snapshot must be saved with reminderSent=true
        assertThat(snapshot.isReminderSent()).isTrue();
        verify(repository).save(snapshot);
    }

    @Test
    void shouldDoNothingWhenNoRemindersArePending() {
        when(repository.findConfirmedWithReminderPending(any(), any())).thenReturn(List.of());

        useCase.execute();

        verify(emailPort, never()).send(anyString(), anyString(), anyString());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldSendRemindersForAllPendingBookings() {
        BookingSnapshot s1 = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("c1").professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusHours(24))
                .endDatetime(LocalDateTime.now().plusHours(25))
                .status("CONFIRMED").updatedAt(LocalDateTime.now())
                .clientEmail("c1@example.com").professionalEmail("p1@example.com")
                .reminderSent(false).build();

        BookingSnapshot s2 = BookingSnapshot.builder()
                .bookingId(UUID.randomUUID())
                .clientId("c2").professionalId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusHours(24))
                .endDatetime(LocalDateTime.now().plusHours(25))
                .status("CONFIRMED").updatedAt(LocalDateTime.now())
                .clientEmail("c2@example.com").professionalEmail("p2@example.com")
                .reminderSent(false).build();

        when(repository.findConfirmedWithReminderPending(any(), any())).thenReturn(List.of(s1, s2));

        useCase.execute();

        // 2 snapshots × 2 emails each = 4 email sends
        verify(emailPort, times(4)).send(anyString(), anyString(), anyString());
        verify(repository, times(2)).save(any());
        assertThat(s1.isReminderSent()).isTrue();
        assertThat(s2.isReminderSent()).isTrue();
    }

    @Test
    void shouldQueryCorrectTimeWindow() {
        when(repository.findConfirmedWithReminderPending(any(), any())).thenReturn(List.of());

        useCase.execute();

        ArgumentCaptor<LocalDateTime> fromCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> toCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(repository).findConfirmedWithReminderPending(fromCaptor.capture(), toCaptor.capture());

        LocalDateTime from = fromCaptor.getValue();
        LocalDateTime to = toCaptor.getValue();

        // Window must be ~23h to ~25h from now
        assertThat(from).isAfterOrEqualTo(LocalDateTime.now().plusHours(22).plusMinutes(59));
        assertThat(to).isAfterOrEqualTo(LocalDateTime.now().plusHours(24).plusMinutes(59));
        assertThat(to).isAfter(from);
    }
}
