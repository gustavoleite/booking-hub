package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_professionals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalEntity {
  @Id
  private UUID id;
  private String name;
  private String bio;
  private String avatarUrl;
  @Builder.Default
  private boolean active = true;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(
      name = "tb_professional_specialties", joinColumns = @JoinColumn(name = "professional_id"))
  @Column(name = "specialty")
  private List<String> specialties;
}
