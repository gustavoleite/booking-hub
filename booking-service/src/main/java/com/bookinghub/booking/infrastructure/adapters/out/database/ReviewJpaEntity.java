package com.bookinghub.booking.infrastructure.adapters.out.database;

import com.bookinghub.booking.core.domain.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_reviews")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewJpaEntity {

  @Id
  private UUID id;

  @Column(name = "booking_id", nullable = false, unique = true)
  private UUID bookingId;

  @Column(name = "client_id", nullable = false)
  private String clientId;

  @Column(name = "professional_id", nullable = false)
  private UUID professionalId;

  @Column(name = "establishment_id", nullable = false)
  private UUID establishmentId;

  @Column(name = "professional_rating")
  private Integer professionalRating;

  @Column(name = "establishment_rating")
  private Integer establishmentRating;

  @Column(name = "comment")
  private String comment;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public static ReviewJpaEntity from(Review domain) {
    return new ReviewJpaEntity(
        domain.getId(),
        domain.getBookingId(),
        domain.getClientId(),
        domain.getProfessionalId(),
        domain.getEstablishmentId(),
        domain.getProfessionalRating(),
        domain.getEstablishmentRating(),
        domain.getComment(),
        domain.getCreatedAt());
  }

  public Review toDomain() {
    return Review.builder()
        .id(id)
        .bookingId(bookingId)
        .clientId(clientId)
        .professionalId(professionalId)
        .establishmentId(establishmentId)
        .professionalRating(professionalRating)
        .establishmentRating(establishmentRating)
        .comment(comment)
        .createdAt(createdAt)
        .build();
  }
}
