package com.bookinghub.booking.core.usecases;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.ports.ReviewRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetReviewsByProfessionalUseCaseTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewsByProfessionalUseCase useCase;

    @Test
    void shouldCalculateAverageSuccessfully() {
        UUID professionalId = UUID.randomUUID();
        List<Review> reviews = List.of(
                Review.builder().professionalRating(5).build(),
                Review.builder().professionalRating(3).build()
        );
        when(reviewRepository.findByProfessionalId(professionalId)).thenReturn(reviews);

        GetReviewsByProfessionalUseCase.Result result = useCase.execute(professionalId);

        assertThat(result.reviews()).hasSize(2);
        assertThat(result.averageRating()).isEqualTo(4.0);
        assertThat(result.totalReviews()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullAverageWhenNoRatings() {
        UUID professionalId = UUID.randomUUID();
        when(reviewRepository.findByProfessionalId(professionalId)).thenReturn(List.of());

        GetReviewsByProfessionalUseCase.Result result = useCase.execute(professionalId);

        assertThat(result.averageRating()).isNull();
        assertThat(result.totalReviews()).isZero();
    }
}
