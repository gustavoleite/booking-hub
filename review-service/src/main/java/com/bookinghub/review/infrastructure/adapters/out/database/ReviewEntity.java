package com.bookinghub.review.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tb_reviews")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private UUID bookingId;

    @Column(nullable = false)
    private String clientId;

    @Column(nullable = false)
    private UUID professionalId;

    @Column(nullable = false)
    private UUID establishmentId;

    private Integer professionalRating;

    private Integer establishmentRating;

    @Column(columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
