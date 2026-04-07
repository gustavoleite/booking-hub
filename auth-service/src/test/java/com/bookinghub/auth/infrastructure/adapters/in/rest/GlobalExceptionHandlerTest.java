package com.bookinghub.auth.infrastructure.adapters.in.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bookinghub.auth.core.exceptions.EmailAlreadyExistsException;
import com.bookinghub.auth.core.exceptions.InactiveUserException;
import com.bookinghub.auth.core.exceptions.InvalidCredentialsException;
import com.bookinghub.auth.core.exceptions.InvalidRoleException;
import com.bookinghub.auth.core.exceptions.WeakPasswordException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler handler;

  @BeforeEach
  void setUp() {
    handler = new GlobalExceptionHandler();
  }

  @Test
  void shouldHandleEmailAlreadyExistsException() {
    ResponseEntity<ProblemDetail> response = handler.handleEmailExists(new EmailAlreadyExistsException("Email exists"));
    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    assertEquals("Email exists", response.getBody().getDetail());
  }

  @Test
  void shouldHandleWeakPasswordException() {
    ResponseEntity<ProblemDetail> response = handler.handleBadRequest(new WeakPasswordException("Weak password"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Weak password", response.getBody().getDetail());
  }

  @Test
  void shouldHandleInvalidRoleException() {
    ResponseEntity<ProblemDetail> response = handler.handleBadRequest(new InvalidRoleException("Invalid role"));
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("Invalid role", response.getBody().getDetail());
  }

  @Test
  void shouldHandleInvalidCredentialsException() {
    ResponseEntity<ProblemDetail> response = handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid credentials"));
    assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    assertEquals("Invalid credentials", response.getBody().getDetail());
  }

  @Test
  void shouldHandleInactiveUserException() {
    ResponseEntity<ProblemDetail> response = handler.handleInactiveUser(new InactiveUserException("User inactive"));
    assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    assertEquals("User inactive", response.getBody().getDetail());
  }

  @Test
  void shouldHandleMethodArgumentNotValidException() {
    BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
    bindingResult.addError(new FieldError("object", "field", "default message"));
    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

    ResponseEntity<ProblemDetail> response = handler.handleValidationErrors(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().getDetail().contains("field: default message"));
  }

  @Test
  void shouldHandleHttpMessageNotReadableException() {
    HttpMessageNotReadableException ex = new HttpMessageNotReadableException("com.bookinghub.auth.core.domain.Role error", new MockHttpInputMessage("".getBytes()));
    ResponseEntity<ProblemDetail> response = handler.handleJsonError(ex);
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertTrue(response.getBody().getDetail().contains("Perfil de usuário inválido"));
  }
}
