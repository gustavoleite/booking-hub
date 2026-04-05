package com.bookinghub.review.core.usecases;

import com.bookinghub.review.core.domain.Review;
import com.bookinghub.review.core.ports.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReviewsByEstablishmentUseCaseTest {

    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewsByEstablishmentUseCase useCase;

    private final UUID establishmentId = UUID.randomUUID();

    @Test
    void shouldReturnReviewsWithAverage() {
        List<Review> reviews = List.of(
                buildReview(null, 5),
                buildReview(null, 4));
        when(reviewRepository.findByEstablishmentId(establishmentId)).thenReturn(reviews);

        GetReviewsByEstablishmentUseCase.Result result = useCase.execute(establishmentId);

        assertThat(result.reviews()).hasSize(2);
        assertThat(result.averageRating()).isEqualTo(4.5);
        assertThat(result.totalReviews()).isEqualTo(2);
    }

    @Test
    void shouldReturnEmptyForUnknownEstablishment() {
        when(reviewRepository.findByEstablishmentId(establishmentId)).thenReturn(List.of());

        GetReviewsByEstablishmentUseCase.Result result = useCase.execute(establishmentId);

        assertThat(result.reviews()).isEmpty();
        assertThat(result.averageRating()).isNull();
    }

    private Review buildReview(Integer professionalRating, Integer establishmentRating) {
        return Review.builder()
                .id(UUID.randomUUID())
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(UUID.randomUUID())
                .establishmentId(establishmentId)
                .professionalRating(professionalRating)
                .establishmentRating(establishmentRating)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
