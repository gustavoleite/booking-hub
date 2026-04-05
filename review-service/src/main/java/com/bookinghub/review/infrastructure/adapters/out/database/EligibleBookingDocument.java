package com.bookinghub.review.infrastructure.adapters.out.database;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "eligible_bookings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibleBookingDocument {

    @Id
    private String bookingId;

    private String clientId;

    private String professionalId;

    private String establishmentId;

    private LocalDateTime completedAt;
}
