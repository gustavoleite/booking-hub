package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostgresReviewRepositoryAdapterTest {

    @Mock
    private JpaReviewRepository jpa;

    @InjectMocks
    private PostgresReviewRepositoryAdapter adapter;

    @Test
    void shouldSaveReview() {
        Review review = Review.builder().id(UUID.randomUUID()).clientId("c1").build();
        ReviewJpaEntity entity = ReviewJpaEntity.from(review);
        when(jpa.save(any())).thenReturn(entity);

        Review result = adapter.save(review);

        assertThat(result.getId()).isEqualTo(review.getId());
    }

    @Test
    void shouldFindByBookingId() {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).bookingId(bookingId).clientId("c1").build();
        when(jpa.findByBookingId(bookingId)).thenReturn(Optional.of(ReviewJpaEntity.from(review)));

        Optional<Review> result = adapter.findByBookingId(bookingId);

        assertThat(result).isPresent();
        assertThat(result.get().getBookingId()).isEqualTo(bookingId);
    }

    @Test
    void shouldFindByProfessionalId() {
        UUID profId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).professionalId(profId).clientId("c1").build();
        when(jpa.findByProfessionalId(profId)).thenReturn(List.of(ReviewJpaEntity.from(review)));

        List<Review> result = adapter.findByProfessionalId(profId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProfessionalId()).isEqualTo(profId);
    }

    @Test
    void shouldFindByEstablishmentId() {
        UUID estId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).establishmentId(estId).clientId("c1").build();
        when(jpa.findByEstablishmentId(estId)).thenReturn(List.of(ReviewJpaEntity.from(review)));

        List<Review> result = adapter.findByEstablishmentId(estId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEstablishmentId()).isEqualTo(estId);
    }

    @Test
    void shouldExistsByBookingId() {
        UUID bookingId = UUID.randomUUID();
        when(jpa.existsByBookingId(bookingId)).thenReturn(true);

        boolean exists = adapter.existsByBookingId(bookingId);

        assertThat(exists).isTrue();
    }
}
