package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.application.dto.CreateReviewRequest;
import com.bookinghub.booking.application.dto.RatingStatsResponse;
import com.bookinghub.booking.application.dto.ReviewListResponse;
import com.bookinghub.booking.application.dto.ReviewResponse;
import com.bookinghub.booking.application.dto.ReviewSummary;
import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.usecases.CreateReviewUseCase;
import com.bookinghub.booking.core.usecases.GetReviewByBookingUseCase;
import com.bookinghub.booking.core.usecases.GetReviewsByEstablishmentUseCase;
import com.bookinghub.booking.core.usecases.GetReviewsByProfessionalUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final CreateReviewUseCase createReviewUseCase;
    private final GetReviewsByProfessionalUseCase getReviewsByProfessionalUseCase;
    private final GetReviewsByEstablishmentUseCase getReviewsByEstablishmentUseCase;
    private final GetReviewByBookingUseCase getReviewByBookingUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse createReview(
            @RequestHeader("X-User-Id") String clientId,
            @Valid @RequestBody CreateReviewRequest request) {
        Review review = createReviewUseCase.execute(
                clientId, request.bookingId(),
                request.professionalRating(), request.establishmentRating(),
                request.comment());
        return ReviewResponse.from(review);
    }

    @GetMapping("/professional/{professionalId}")
    public ReviewListResponse getByProfessional(
            @PathVariable("professionalId") UUID professionalId) {
        GetReviewsByProfessionalUseCase.Result result =
                getReviewsByProfessionalUseCase.execute(professionalId);
        List<ReviewSummary> summaries = result.reviews().stream()
                .map(ReviewSummary::from).toList();
        return new ReviewListResponse(summaries, result.averageRating(), result.totalReviews());
    }

    @GetMapping("/professional/{professionalId}/stats")
    public RatingStatsResponse getProfessionalStats(
            @PathVariable("professionalId") UUID professionalId) {
        GetReviewsByProfessionalUseCase.Result result =
                getReviewsByProfessionalUseCase.execute(professionalId);
        return new RatingStatsResponse(
                professionalId, result.averageRating(), result.totalReviews());
    }

    @GetMapping("/establishment/{establishmentId}")
    public ReviewListResponse getByEstablishment(
            @PathVariable("establishmentId") UUID establishmentId) {
        GetReviewsByEstablishmentUseCase.Result result =
                getReviewsByEstablishmentUseCase.execute(establishmentId);
        List<ReviewSummary> summaries = result.reviews().stream()
                .map(ReviewSummary::from).toList();
        return new ReviewListResponse(summaries, result.averageRating(), result.totalReviews());
    }

    @GetMapping("/establishment/{establishmentId}/stats")
    public RatingStatsResponse getEstablishmentStats(
            @PathVariable("establishmentId") UUID establishmentId) {
        GetReviewsByEstablishmentUseCase.Result result =
                getReviewsByEstablishmentUseCase.execute(establishmentId);
        return new RatingStatsResponse(
                establishmentId, result.averageRating(), result.totalReviews());
    }

    @GetMapping("/booking/{bookingId}")
    public ReviewResponse getByBooking(
            @PathVariable("bookingId") UUID bookingId,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader("X-User-Role") String requesterRole) {
        Review review = getReviewByBookingUseCase.execute(bookingId, requesterId, requesterRole);
        return ReviewResponse.from(review);
    }
}
