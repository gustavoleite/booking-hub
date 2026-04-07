package com.bookinghub.auth.infrastructure.adapters.in.rest;

import com.bookinghub.auth.core.exceptions.EmailAlreadyExistsException;
import com.bookinghub.auth.core.exceptions.InactiveUserException;
import com.bookinghub.auth.core.exceptions.InvalidCredentialsException;
import com.bookinghub.auth.core.exceptions.InvalidRoleException;
import com.bookinghub.auth.core.exceptions.WeakPasswordException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(EmailAlreadyExistsException.class)
  public ResponseEntity<ProblemDetail> handleEmailExists(EmailAlreadyExistsException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problem.setTitle("Conflito de Dados");
    return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
  }

  @ExceptionHandler({WeakPasswordException.class, InvalidRoleException.class})
  public ResponseEntity<ProblemDetail> handleBadRequest(RuntimeException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problem.setTitle("Dados de Entrada Inválidos");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(InvalidCredentialsException.class)
  public ResponseEntity<ProblemDetail> handleInvalidCredentials(InvalidCredentialsException ex) {
    ProblemDetail problem =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
    problem.setTitle("Falha na Autenticação");
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(problem);
  }

  @ExceptionHandler(InactiveUserException.class)
  public ResponseEntity<ProblemDetail> handleInactiveUser(InactiveUserException ex) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    problem.setTitle("Usuário Inativo");
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ProblemDetail> handleValidationErrors(MethodArgumentNotValidException ex) {
    String detail = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problem.setTitle("Erro de Validação");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ProblemDetail> handleJsonError(HttpMessageNotReadableException ex) {
    String message = ex.getMessage();
    if (message != null && message.contains("com.bookinghub.auth.core.domain.Role")) {
      message = "Perfil de usuário inválido. Valores permitidos: "
          + "ROLE_CLIENT, ROLE_PROFESSIONAL, ROLE_OWNER.";
    }
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
    problem.setTitle("Dados de Entrada Inválidos");
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
  }
}
