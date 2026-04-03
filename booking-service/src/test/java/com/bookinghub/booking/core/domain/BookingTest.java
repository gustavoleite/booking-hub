package com.bookinghub.booking.core.domain;

import com.bookinghub.booking.core.exceptions.BookingStatusException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingTest {

    private Booking buildConfirmedBooking() {
        return Booking.create("client1", UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), LocalDateTime.now().plusDays(1),
                new BigDecimal("50.00"), 60, null);
    }

    @Test
    void shouldCreateBookingWithConfirmedStatus() {
        Booking booking = buildConfirmedBooking();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.getEndDatetime()).isEqualTo(booking.getStartDatetime().plusMinutes(60));
        assertThat(booking.getId()).isNotNull();
        assertThat(booking.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCancelConfirmedBooking() {
        Booking booking = buildConfirmedBooking();
        booking.cancel("test reason");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getCancelReason()).isEqualTo("test reason");
        assertThat(booking.getCancelledAt()).isNotNull();
    }

    @Test
    void shouldCompleteConfirmedBooking() {
        Booking booking = buildConfirmedBooking();
        booking.complete();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.COMPLETED);
    }

    @Test
    void shouldMarkNoShowOnConfirmedBooking() {
        Booking booking = buildConfirmedBooking();
        booking.markNoShow();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.NO_SHOW);
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledBooking() {
        Booking booking = buildConfirmedBooking();
        booking.cancel("first");
        assertThatThrownBy(() -> booking.cancel("second"))
                .isInstanceOf(BookingStatusException.class);
    }

    @Test
    void shouldThrowWhenCompletingCancelledBooking() {
        Booking booking = buildConfirmedBooking();
        booking.cancel("reason");
        assertThatThrownBy(booking::complete)
                .isInstanceOf(BookingStatusException.class);
    }

    @Test
    void isOwnedByShouldReturnTrueForSameClient() {
        Booking booking = buildConfirmedBooking();
        assertThat(booking.isOwnedBy("client1")).isTrue();
        assertThat(booking.isOwnedBy("other")).isFalse();
    }
}
