package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.application.dto.BookingResponse;
import com.bookinghub.booking.application.dto.CancelBookingRequest;
import com.bookinghub.booking.application.dto.CreateBookingRequest;
import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.usecases.CancelBookingUseCase;
import com.bookinghub.booking.core.usecases.CompleteBookingUseCase;
import com.bookinghub.booking.core.usecases.CreateBookingUseCase;
import com.bookinghub.booking.core.usecases.GetBookingDetailsUseCase;
import com.bookinghub.booking.core.usecases.ListClientBookingsUseCase;
import com.bookinghub.booking.core.usecases.ListEstablishmentBookingsUseCase;
import com.bookinghub.booking.core.usecases.ListProfessionalAgendaUseCase;
import com.bookinghub.booking.core.usecases.MarkNoShowUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings")
public class BookingController {

  private final CreateBookingUseCase createBookingUseCase;
  private final GetBookingDetailsUseCase getBookingDetailsUseCase;
  private final CancelBookingUseCase cancelBookingUseCase;
  private final CompleteBookingUseCase completeBookingUseCase;
  private final MarkNoShowUseCase markNoShowUseCase;
  private final ListClientBookingsUseCase listClientBookingsUseCase;
  private final ListProfessionalAgendaUseCase listProfessionalAgendaUseCase;
  private final ListEstablishmentBookingsUseCase listEstablishmentBookingsUseCase;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Create a booking")
  public BookingResponse create(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateBookingRequest request) {
    Booking booking = createBookingUseCase.execute(
        userId,
        request.professionalId(),
        request.establishmentId(),
        request.providedServiceId(),
        request.startDatetime(),
        request.notes()
    );
    return BookingResponse.from(booking);
  }

  @GetMapping("/me")
  @Operation(summary = "List bookings for the authenticated client")
  public List<BookingResponse> listMyBookings(@RequestHeader("X-User-Id") String userId) {
    return listClientBookingsUseCase.execute(userId)
        .stream().map(BookingResponse::from).toList();
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get booking details")
  public BookingResponse getById(
            @PathVariable("id") UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {
    return BookingResponse.from(getBookingDetailsUseCase.execute(id, userId, role));
  }

  @PatchMapping("/{id}/cancel")
  @Operation(summary = "Cancel a booking")
  public BookingResponse cancel(
            @PathVariable("id") UUID id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestBody(required = false) CancelBookingRequest request) {
    String reason = request != null ? request.reason() : null;
    return BookingResponse.from(cancelBookingUseCase.execute(id, userId, role, reason));
  }

  @PatchMapping("/{id}/complete")
  @Operation(summary = "Mark a booking as completed")
  public BookingResponse complete(
            @PathVariable("id") UUID id,
            @RequestHeader("X-User-Role") String role) {
    return BookingResponse.from(completeBookingUseCase.execute(id, role));
  }

  @PatchMapping("/{id}/no-show")
  @Operation(summary = "Mark a booking as no-show")
  public BookingResponse noShow(
            @PathVariable("id") UUID id,
            @RequestHeader("X-User-Role") String role) {
    return BookingResponse.from(markNoShowUseCase.execute(id, role));
  }

  @GetMapping("/professional")
  @Operation(summary = "List agenda for the authenticated professional")
  public List<BookingResponse> professionalAgenda(@RequestHeader("X-User-Id") String userId) {
    return listProfessionalAgendaUseCase.execute(UUID.fromString(userId))
        .stream().map(BookingResponse::from).toList();
  }

  @GetMapping("/establishment/{establishmentId}")
  @Operation(summary = "List all bookings for an establishment")
  public List<BookingResponse> establishmentBookings(
      @PathVariable("establishmentId") UUID establishmentId) {
    return listEstablishmentBookingsUseCase.execute(establishmentId)
        .stream().map(BookingResponse::from).toList();
  }
}
