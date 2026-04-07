package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.EligibleBooking;
import com.bookinghub.booking.core.ports.EligibleBookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumeBookingCompletedUseCaseTest {

    @Mock
    private EligibleBookingRepository eligibleBookingRepository;

    @InjectMocks
    private ConsumeBookingCompletedUseCase useCase;

    @Test
    void shouldSaveEligibleBookingWhenNotExists() {
        UUID bookingId = UUID.randomUUID();
        String clientId = "client-1";
        UUID professionalId = UUID.randomUUID();
        UUID establishmentId = UUID.randomUUID();
        LocalDateTime completedAt = LocalDateTime.now();

        when(eligibleBookingRepository.existsById(bookingId)).thenReturn(false);

        useCase.execute(bookingId, clientId, professionalId, establishmentId, completedAt);

        verify(eligibleBookingRepository).save(any(EligibleBooking.class));
    }

    @Test
    void shouldNotSaveWhenAlreadyExists() {
        UUID bookingId = UUID.randomUUID();
        when(eligibleBookingRepository.existsById(bookingId)).thenReturn(true);

        useCase.execute(bookingId, "c", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());

        verify(eligibleBookingRepository, never()).save(any());
    }
}
