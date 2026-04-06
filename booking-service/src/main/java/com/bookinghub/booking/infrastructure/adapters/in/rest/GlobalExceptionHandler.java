package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.core.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleNotFound(BookingNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ForbiddenBookingAccessException.class)
    public ProblemDetail handleForbidden(ForbiddenBookingAccessException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(SlotUnavailableException.class)
    public ProblemDetail handleSlotUnavailable(SlotUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(BookingStatusException.class)
    public ProblemDetail handleBookingStatus(BookingStatusException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(CatalogServiceException.class)
    public ProblemDetail handleCatalogService(CatalogServiceException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    @ExceptionHandler(com.bookinghub.booking.core.exceptions.BookingNotEligibleException.class)
    public ProblemDetail handleBookingNotEligible(com.bookinghub.booking.core.exceptions.BookingNotEligibleException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException.class)
    public ProblemDetail handleForbiddenReview(com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(com.bookinghub.booking.core.exceptions.InvalidReviewException.class)
    public ProblemDetail handleInvalidReview(com.bookinghub.booking.core.exceptions.InvalidReviewException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(com.bookinghub.booking.core.exceptions.ReviewAlreadyExistsException.class)
    public ProblemDetail handleReviewAlreadyExists(com.bookinghub.booking.core.exceptions.ReviewAlreadyExistsException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(com.bookinghub.booking.core.exceptions.ReviewNotFoundException.class)
    public ProblemDetail handleReviewNotFound(com.bookinghub.booking.core.exceptions.ReviewNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }
}
