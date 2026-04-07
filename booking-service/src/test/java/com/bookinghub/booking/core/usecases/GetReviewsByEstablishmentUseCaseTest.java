package com.bookinghub.booking.core.usecases;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.ports.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetReviewsByEstablishmentUseCaseTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private GetReviewsByEstablishmentUseCase useCase;

    @Test
    void shouldCalculateAverageSuccessfully() {
        UUID establishmentId = UUID.randomUUID();
        List<Review> reviews = List.of(
                Review.builder().establishmentRating(5).build(),
                Review.builder().establishmentRating(4).build(),
                Review.builder().establishmentRating(null).build()
        );
        when(reviewRepository.findByEstablishmentId(establishmentId)).thenReturn(reviews);

        GetReviewsByEstablishmentUseCase.Result result = useCase.execute(establishmentId);

        assertThat(result.reviews()).hasSize(3);
        assertThat(result.averageRating()).isEqualTo(4.5);
        assertThat(result.totalReviews()).isEqualTo(2);
    }

    @Test
    void shouldReturnNullAverageWhenNoRatings() {
        UUID establishmentId = UUID.randomUUID();
        when(reviewRepository.findByEstablishmentId(establishmentId)).thenReturn(List.of());

        GetReviewsByEstablishmentUseCase.Result result = useCase.execute(establishmentId);

        assertThat(result.averageRating()).isNull();
        assertThat(result.totalReviews()).isZero();
    }
}
