package com.bookinghub.booking.infrastructure.adapters.in.rest;

import com.bookinghub.booking.core.domain.Booking;
import com.bookinghub.booking.core.domain.BookingStatus;
import com.bookinghub.booking.core.usecases.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateBookingUseCase createBookingUseCase;
    @MockBean
    private GetBookingDetailsUseCase getBookingDetailsUseCase;
    @MockBean
    private CancelBookingUseCase cancelBookingUseCase;
    @MockBean
    private CompleteBookingUseCase completeBookingUseCase;
    @MockBean
    private MarkNoShowUseCase markNoShowUseCase;
    @MockBean
    private ListClientBookingsUseCase listClientBookingsUseCase;
    @MockBean
    private ListProfessionalAgendaUseCase listProfessionalAgendaUseCase;
    @MockBean
    private ListEstablishmentBookingsUseCase listEstablishmentBookingsUseCase;

    private Booking buildBooking() {
        return Booking.builder()
                .id(UUID.randomUUID())
                .clientId("client-id")
                .professionalId(UUID.randomUUID())
                .establishmentId(UUID.randomUUID())
                .providedServiceId(UUID.randomUUID())
                .startDatetime(LocalDateTime.now().plusDays(1))
                .endDatetime(LocalDateTime.now().plusDays(1).plusHours(1))
                .status(BookingStatus.CONFIRMED)
                .price(new BigDecimal("100.00"))
                .durationMinutes(60)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldCreateBooking() throws Exception {
        Booking booking = buildBooking();
        when(createBookingUseCase.execute(any(), any(), any(), any(), any(), any())).thenReturn(booking);

        String json = """
                {
                    "professionalId": "%s",
                    "establishmentId": "%s",
                    "providedServiceId": "%s",
                    "startDatetime": "%s"
                }
                """.formatted(booking.getProfessionalId(), booking.getEstablishmentId(), booking.getProvidedServiceId(), booking.getStartDatetime());

        mockMvc.perform(post("/bookings")
                        .header("X-User-Id", "client-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()));
    }

    @Test
    void shouldListMyBookings() throws Exception {
        Booking booking = buildBooking();
        when(listClientBookingsUseCase.execute("client-id")).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/me")
                        .header("X-User-Id", "client-id"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId().toString()));
    }

    @Test
    void shouldGetById() throws Exception {
        Booking booking = buildBooking();
        when(getBookingDetailsUseCase.execute(eq(booking.getId()), eq("client-id"), eq("ROLE_CLIENT"))).thenReturn(booking);

        mockMvc.perform(get("/bookings/" + booking.getId())
                        .header("X-User-Id", "client-id")
                        .header("X-User-Role", "ROLE_CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(booking.getId().toString()));
    }

    @Test
    void shouldCancelBooking() throws Exception {
        Booking booking = buildBooking();
        booking.cancel("reason");
        when(cancelBookingUseCase.execute(eq(booking.getId()), eq("client-id"), eq("ROLE_CLIENT"), eq("reason"))).thenReturn(booking);

        mockMvc.perform(patch("/bookings/" + booking.getId() + "/cancel")
                        .header("X-User-Id", "client-id")
                        .header("X-User-Role", "ROLE_CLIENT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"reason\": \"reason\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void shouldCompleteBooking() throws Exception {
        Booking booking = buildBooking();
        booking.complete();
        when(completeBookingUseCase.execute(eq(booking.getId()), eq("ROLE_PROFESSIONAL"))).thenReturn(booking);

        mockMvc.perform(patch("/bookings/" + booking.getId() + "/complete")
                        .header("X-User-Role", "ROLE_PROFESSIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void shouldMarkNoShow() throws Exception {
        Booking booking = buildBooking();
        booking.markNoShow();
        when(markNoShowUseCase.execute(eq(booking.getId()), eq("ROLE_PROFESSIONAL"))).thenReturn(booking);

        mockMvc.perform(patch("/bookings/" + booking.getId() + "/no-show")
                        .header("X-User-Role", "ROLE_PROFESSIONAL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    void shouldListProfessionalAgenda() throws Exception {
        Booking booking = buildBooking();
        UUID profId = UUID.randomUUID();
        when(listProfessionalAgendaUseCase.execute(profId)).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/professional")
                        .header("X-User-Id", profId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId().toString()));
    }

    @Test
    void shouldListEstablishmentBookings() throws Exception {
        Booking booking = buildBooking();
        UUID estId = UUID.randomUUID();
        when(listEstablishmentBookingsUseCase.execute(estId)).thenReturn(List.of(booking));

        mockMvc.perform(get("/bookings/establishment/" + estId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(booking.getId().toString()));
    }
}
