package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.application.dto.AvailabilityResponse;
import com.bookinghub.booking.core.usecases.GetAvailableSlotsUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bookings/availability")
@RequiredArgsConstructor
@Tag(name = "Availability")
public class AvailabilityController {

  private final GetAvailableSlotsUseCase getAvailableSlotsUseCase;

  @GetMapping
  @Operation(summary = "Get available booking slots (public endpoint)")
  public AvailabilityResponse getAvailability(
            @RequestParam("establishmentId") UUID establishmentId,
            @RequestParam("professionalId") UUID professionalId,
            @RequestParam("serviceId") UUID serviceId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

    GetAvailableSlotsUseCase.Result result =
        getAvailableSlotsUseCase.execute(establishmentId, professionalId, serviceId, date);

    return new AvailabilityResponse(
        establishmentId,
        professionalId,
        serviceId,
        result.durationMinutes(),
        result.price(),
        result.availableSlots()
    );
  }
}
