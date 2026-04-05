package com.bookinghub.review.infrastructure.adapters.out.database;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String bookingId;

    private String clientId;

    @Indexed
    private String professionalId;

    @Indexed
    private String establishmentId;

    private Integer professionalRating;

    private Integer establishmentRating;

    private String comment;

    private LocalDateTime createdAt;
}
