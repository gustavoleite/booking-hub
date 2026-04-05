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
class GetReviewsByProfessionalUseCaseTest {

    @Mock private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewsByProfessionalUseCase useCase;

    private final UUID professionalId = UUID.randomUUID();

    @Test
    void shouldReturnReviewsWithAverage() {
        List<Review> reviews = List.of(
                buildReview(5, null),
                buildReview(3, null),
                buildReview(4, null));
        when(reviewRepository.findByProfessionalId(professionalId)).thenReturn(reviews);

        GetReviewsByProfessionalUseCase.Result result = useCase.execute(professionalId);

        assertThat(result.reviews()).hasSize(3);
        assertThat(result.totalReviews()).isEqualTo(3);
        assertThat(result.averageRating()).isEqualTo(4.0);
    }

    @Test
    void shouldReturnNullAverageWhenNoProfessionalRatings() {
        List<Review> reviews = List.of(buildReview(null, 5));
        when(reviewRepository.findByProfessionalId(professionalId)).thenReturn(reviews);

        GetReviewsByProfessionalUseCase.Result result = useCase.execute(professionalId);

        assertThat(result.averageRating()).isNull();
        assertThat(result.totalReviews()).isEqualTo(0);
    }

    @Test
    void shouldReturnEmptyResultForUnknownProfessional() {
        when(reviewRepository.findByProfessionalId(professionalId)).thenReturn(List.of());

        GetReviewsByProfessionalUseCase.Result result = useCase.execute(professionalId);

        assertThat(result.reviews()).isEmpty();
        assertThat(result.averageRating()).isNull();
        assertThat(result.totalReviews()).isEqualTo(0);
    }

    private Review buildReview(Integer professionalRating, Integer establishmentRating) {
        return Review.builder()
                .id(UUID.randomUUID())
                .bookingId(UUID.randomUUID())
                .clientId("client-1")
                .professionalId(professionalId)
                .establishmentId(UUID.randomUUID())
                .professionalRating(professionalRating)
                .establishmentRating(establishmentRating)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
