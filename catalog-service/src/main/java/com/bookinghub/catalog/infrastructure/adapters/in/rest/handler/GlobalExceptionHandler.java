package com.bookinghub.catalog.infrastructure.adapters.in.rest.handler;

import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessRuleException.class)
  public ProblemDetail handleBusinessRuleException(BusinessRuleException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Business Rule Violation");
    problemDetail.setType(URI.create("https://bookinghub.com/errors/business-rule-violation"));
    return problemDetail;
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFoundException(NotFoundException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Resource Not Found");
    return problemDetail;
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflictException(ConflictException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Conflito de Dados");
    return problemDetail;
  }

  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbiddenException(ForbiddenException ex) {
    ProblemDetail problemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    problemDetail.setTitle("Access Denied");
    return problemDetail;
  }

  @ExceptionHandler(MissingRequestHeaderException.class)
  public ProblemDetail handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "Required header missing: " + ex.getHeaderName());
    problemDetail.setTitle("Missing Request Header");
    return problemDetail;
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ProblemDetail handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST, "JSON parse error: " + ex.getMessage());
    problemDetail.setTitle("Malformed JSON Request");
    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneralException(Exception ex) {
    ex.printStackTrace();
    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred: " + ex.getMessage());
    problemDetail.setTitle("Internal Server Error");
    return problemDetail;
  }
}
