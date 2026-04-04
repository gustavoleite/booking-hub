package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresBookingRepositoryAdapterTest {

    @Mock
    private JpaBookingRepository jpaRepository;

    @InjectMocks
    private PostgresBookingRepositoryAdapter adapter;

    private Booking buildBooking() {
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientId("client1")
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .providedServiceId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusDays(1))
                .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .price(new BigDecimal("50.00"))
                .durationMinutes(60)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private BookingEntity buildEntity(Booking b) {
        return BookingEntity.builder()
                .id(b.getId())
                .clientId(b.getClientId())
                .professionalId(b.getProfessionalId())
                .establishmentId(b.getEstablishmentId())
                .providedServiceId(b.getProvidedServiceId())
                .startDatetime(b.getStartDatetime())
                .endDatetime(b.getEndDatetime())
                .status(b.getStatus())
                .price(b.getPrice())
                .durationMinutes(b.getDurationMinutes())
                .createdAt(b.getCreatedAt())
                .build();
    }

    @Test
    void shouldSaveBooking() {
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.save(any())).thenReturn(entity);

        Booking result = adapter.save(booking);

        assertThat(result.getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldFindById() {
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.findById(booking.getId())).thenReturn(Optional.of(entity));

        Optional<Booking> result = adapter.findById(booking.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(booking.getId());
    }

    @Test
    void shouldExistsActiveSlot() {
        UUID profId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(jpaRepository.existsActiveSlot(profId, now)).thenReturn(true);

        boolean exists = adapter.existsActiveSlot(profId, now);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldFindByClientId() {
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.findByClientIdOrderByStartDatetimeDesc("client1")).thenReturn(List.of(entity));

        List<Booking> results = adapter.findByClientId("client1");

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldFindByProfessionalId() {
        UUID profId = UUID.randomUUID();
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.findByProfessionalIdOrderByStartDatetimeDesc(profId)).thenReturn(List.of(entity));

        List<Booking> results = adapter.findByProfessionalId(profId);

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldFindByEstablishmentId() {
        UUID estId = UUID.randomUUID();
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.findByEstablishmentIdOrderByStartDatetimeDesc(estId)).thenReturn(List.of(entity));

        List<Booking> results = adapter.findByEstablishmentId(estId);

        assertThat(results).hasSize(1);
    }

    @Test
    void shouldFindByProfessionalAndDate() {
        UUID profId = UUID.randomUUID();
        LocalDate date = LocalDate.now();
        Booking booking = buildBooking();
        BookingEntity entity = buildEntity(booking);
        when(jpaRepository.findByProfessionalAndDate(any(), any(), any())).thenReturn(List.of(entity));

        List<Booking> results = adapter.findByProfessionalAndDate(profId, date);

        assertThat(results).hasSize(1);
    }
}
