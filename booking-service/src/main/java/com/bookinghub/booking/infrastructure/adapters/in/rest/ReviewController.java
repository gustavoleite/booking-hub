package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.application.dto.*;
import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.usecases.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
        return new RatingStatsResponse(professionalId, result.averageRating(), result.totalReviews());
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
        return new RatingStatsResponse(establishmentId, result.averageRating(), result.totalReviews());
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
