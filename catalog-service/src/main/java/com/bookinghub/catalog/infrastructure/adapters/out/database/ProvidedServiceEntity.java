package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_provided_services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvidedServiceEntity {
  @Id
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "establishment_id")
  private EstablishmentEntity establishment;

  private String title;
  private String description;
  @Builder.Default
  private boolean active = true;
}
