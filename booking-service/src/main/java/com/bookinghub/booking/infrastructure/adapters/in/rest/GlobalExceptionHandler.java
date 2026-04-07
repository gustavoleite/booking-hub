package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.core.exceptions.BookingNotEligibleException;
import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.BookingStatusException;
import com.bookinghub.booking.core.exceptions.CatalogServiceException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.exceptions.ForbiddenReviewAccessException;
import com.bookinghub.booking.core.exceptions.InvalidReviewException;
import com.bookinghub.booking.core.exceptions.ReviewAlreadyExistsException;
import com.bookinghub.booking.core.exceptions.ReviewNotFoundException;
import com.bookinghub.booking.core.exceptions.SlotUnavailableException;
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

  @ExceptionHandler(BookingNotEligibleException.class)
  public ProblemDetail handleBookingNotEligible(BookingNotEligibleException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
  }

  @ExceptionHandler(ForbiddenReviewAccessException.class)
  public ProblemDetail handleForbiddenReview(ForbiddenReviewAccessException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
  }

  @ExceptionHandler(InvalidReviewException.class)
  public ProblemDetail handleInvalidReview(InvalidReviewException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(ReviewAlreadyExistsException.class)
  public ProblemDetail handleReviewAlreadyExists(ReviewAlreadyExistsException ex) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
  }

  @ExceptionHandler(ReviewNotFoundException.class)
  public ProblemDetail handleReviewNotFound(ReviewNotFoundException ex) {
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
    return ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
  }
}
