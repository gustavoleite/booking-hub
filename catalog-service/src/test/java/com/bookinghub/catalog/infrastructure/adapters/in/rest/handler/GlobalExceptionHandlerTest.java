package com.bookinghub.catalog.infrastructure.adapters.in.rest.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.exceptions.ForbiddenException;
import com.bookinghub.catalog.core.exceptions.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void shouldHandleBusinessRuleException() {
    BusinessRuleException ex = new BusinessRuleException("Error");
    ProblemDetail detail = handler.handleBusinessRuleException(ex);
    assertEquals(HttpStatus.BAD_REQUEST.value(), detail.getStatus());
    assertEquals("Error", detail.getDetail());
  }

  @Test
  void shouldHandleNotFoundException() {
    NotFoundException ex = new NotFoundException("Not Found");
    ProblemDetail detail = handler.handleNotFoundException(ex);
    assertEquals(HttpStatus.NOT_FOUND.value(), detail.getStatus());
    assertEquals("Not Found", detail.getDetail());
  }

  @Test
  void shouldHandleConflictException() {
    ConflictException ex = new ConflictException("Conflict");
    ProblemDetail detail = handler.handleConflictException(ex);
    assertEquals(HttpStatus.CONFLICT.value(), detail.getStatus());
    assertEquals("Conflict", detail.getDetail());
  }

  @Test
  void shouldHandleForbiddenException() {
    ForbiddenException ex = new ForbiddenException("Forbidden");
    ProblemDetail detail = handler.handleForbiddenException(ex);
    assertEquals(HttpStatus.FORBIDDEN.value(), detail.getStatus());
    assertEquals("Forbidden", detail.getDetail());
  }

  @Test
  void shouldHandleGeneralException() {
    Exception ex = new Exception("General");
    ProblemDetail detail = handler.handleGeneralException(ex);
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), detail.getStatus());
    assertTrue(detail.getDetail().contains("General"));
  }
}
