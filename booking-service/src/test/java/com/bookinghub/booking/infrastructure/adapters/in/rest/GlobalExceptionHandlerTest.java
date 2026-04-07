package com.bookinghub.booking.infrastructure.adapters.in.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bookinghub.booking.core.exceptions.BookingNotFoundException;
import com.bookinghub.booking.core.exceptions.BookingStatusException;
import com.bookinghub.booking.core.exceptions.CatalogServiceException;
import com.bookinghub.booking.core.exceptions.ForbiddenBookingAccessException;
import com.bookinghub.booking.core.exceptions.SlotUnavailableException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBookingNotFound() {
        BookingNotFoundException ex = new BookingNotFoundException("Not found");
        ProblemDetail result = handler.handleNotFound(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getDetail()).isEqualTo("Not found");
    }

    @Test
    void shouldHandleForbiddenBookingAccess() {
        ForbiddenBookingAccessException ex = new ForbiddenBookingAccessException("Forbidden");
        ProblemDetail result = handler.handleForbidden(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(result.getDetail()).isEqualTo("Forbidden");
    }

    @Test
    void shouldHandleSlotUnavailable() {
        SlotUnavailableException ex = new SlotUnavailableException("Slot unavailable");
        ProblemDetail result = handler.handleSlotUnavailable(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(result.getDetail()).isEqualTo("Slot unavailable");
    }

    @Test
    void shouldHandleBookingStatus() {
        BookingStatusException ex = new BookingStatusException("Invalid status");
        ProblemDetail result = handler.handleBookingStatus(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.value());
        assertThat(result.getDetail()).isEqualTo("Invalid status");
    }

    @Test
    void shouldHandleCatalogService() {
        CatalogServiceException ex = new CatalogServiceException("Catalog error");
        ProblemDetail result = handler.handleCatalogService(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(result.getDetail()).isEqualTo("Catalog error");
    }

    @Test
    void shouldHandleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("obj", "field1", "error1"),
                new FieldError("obj", "field2", "error2")
        ));

        ProblemDetail result = handler.handleValidation(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getDetail()).contains("field1: error1").contains("field2: error2");
    }

    @Test
    void shouldHandleGeneric() {
        Exception ex = new RuntimeException("Generic error");
        ProblemDetail result = handler.handleGeneric(ex);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getDetail()).isEqualTo("An unexpected error occurred");
    }
}
