package com.bookinghub.review.core.usecases;

import com.bookinghub.review.core.ports.EligibleBookingRepository;
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
    void shouldPersistEligibleBooking() {
        UUID bookingId = UUID.randomUUID();
        when(eligibleBookingRepository.existsById(bookingId)).thenReturn(false);

        useCase.execute(bookingId, "client-1", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());

        verify(eligibleBookingRepository).save(any());
    }

    @Test
    void shouldBeIdempotentWhenBookingAlreadyRegistered() {
        UUID bookingId = UUID.randomUUID();
        when(eligibleBookingRepository.existsById(bookingId)).thenReturn(true);

        useCase.execute(bookingId, "client-1", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());

        verify(eligibleBookingRepository, never()).save(any());
    }
}
