package com.bookinghub.catalog.infrastructure.adapters.out.database;

import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresProfessionalRepositoryAdapter implements ProfessionalRepository {
  private final JpaProfessionalRepository jpaRepository;

  @Override
  public Professional save(Professional professional) {
    ProfessionalEntity entity = jpaRepository.findById(professional.getId())
        .orElse(toEntity(professional));
    updateEntity(entity, professional);
    ProfessionalEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  public Optional<Professional> findById(UUID id) {
    return jpaRepository.findByIdAndActiveTrue(id).map(this::toDomain);
  }

  private void updateEntity(ProfessionalEntity entity, Professional domain) {
    entity.setName(domain.getName());
    entity.setBio(domain.getBio());
    entity.setAvatarUrl(domain.getAvatarUrl());
    entity.setActive(domain.isActive());
    entity.setSpecialties(toSafeList(domain.getSpecialties()));
  }

  private ProfessionalEntity toEntity(Professional domain) {
    return ProfessionalEntity.builder()
        .id(domain.getId())
        .name(domain.getName())
        .bio(domain.getBio())
        .avatarUrl(domain.getAvatarUrl())
        .active(domain.isActive())
        .specialties(toSafeList(domain.getSpecialties()))
        .build();
  }

  private List<String> toSafeList(List<String> list) {
    return list != null ? list : Collections.emptyList();
  }

  private Professional toDomain(ProfessionalEntity entity) {
    return Professional.builder()
        .id(entity.getId())
        .name(entity.getName())
        .bio(entity.getBio())
        .avatarUrl(entity.getAvatarUrl())
        .active(entity.isActive())
        .specialties(entity.getSpecialties())
        .build();
  }
}
