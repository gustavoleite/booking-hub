package com.bookinghub.booking.infrastructure.adapters.in.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookinghub.booking.core.domain.Review;
import com.bookinghub.booking.core.usecases.CreateReviewUseCase;
import com.bookinghub.booking.core.usecases.GetReviewByBookingUseCase;
import com.bookinghub.booking.core.usecases.GetReviewsByEstablishmentUseCase;
import com.bookinghub.booking.core.usecases.GetReviewsByProfessionalUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateReviewUseCase createReviewUseCase;
    @MockBean
    private GetReviewsByProfessionalUseCase getReviewsByProfessionalUseCase;
    @MockBean
    private GetReviewsByEstablishmentUseCase getReviewsByEstablishmentUseCase;
    @MockBean
    private GetReviewByBookingUseCase getReviewByBookingUseCase;

    @Test
    void shouldCreateReview() throws Exception {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).bookingId(bookingId).clientId("c1").build();
        when(createReviewUseCase.execute(eq("c1"), eq(bookingId), eq(5), eq(5), any())).thenReturn(review);

        String json = """
                {
                    "bookingId": "%s",
                    "professionalRating": 5,
                    "establishmentRating": 5,
                    "comment": "Nice"
                }
                """.formatted(bookingId);

        mockMvc.perform(post("/reviews")
                .header("X-User-Id", "c1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
    }

    @Test
    void shouldGetByProfessional() throws Exception {
        UUID profId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).professionalRating(5).build();
        when(getReviewsByProfessionalUseCase.execute(profId))
                .thenReturn(new GetReviewsByProfessionalUseCase.Result(List.of(review), 5.0, 1L));

        mockMvc.perform(get("/reviews/professional/" + profId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].professionalRating").value(5));
    }

    @Test
    void shouldGetByEstablishment() throws Exception {
        UUID estId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).establishmentRating(4).build();
        when(getReviewsByEstablishmentUseCase.execute(estId))
                .thenReturn(new GetReviewsByEstablishmentUseCase.Result(List.of(review), 4.0, 1L));

        mockMvc.perform(get("/reviews/establishment/" + estId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviews[0].establishmentRating").value(4));
    }

    @Test
    void shouldGetByBooking() throws Exception {
        UUID bookingId = UUID.randomUUID();
        Review review = Review.builder().id(UUID.randomUUID()).bookingId(bookingId).clientId("c1").build();
        when(getReviewByBookingUseCase.execute(eq(bookingId), eq("c1"), eq("ROLE_CLIENT"))).thenReturn(review);

        mockMvc.perform(get("/reviews/booking/" + bookingId)
                .header("X-User-Id", "c1")
                .header("X-User-Role", "ROLE_CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookingId").value(bookingId.toString()));
    }
}
