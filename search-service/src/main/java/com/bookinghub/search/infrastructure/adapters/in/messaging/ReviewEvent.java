package com.bookinghub.search.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReviewEvent(
        String reviewId,
        String bookingId,
        String clientId,
        String professionalId,
        String establishmentId,
        Double professionalRating,
        Double establishmentRating
) {
}
