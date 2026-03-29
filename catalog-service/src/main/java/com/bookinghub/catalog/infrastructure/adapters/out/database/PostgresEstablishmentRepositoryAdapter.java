package com.bookinghub.catalog.infrastructure.adapters.out.database;

import com.bookinghub.catalog.core.domain.*;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PostgresEstablishmentRepositoryAdapter implements EstablishmentRepository {
    private final JpaEstablishmentRepository jpaRepository;

    @Override
    public Establishment save(Establishment establishment) {
        EstablishmentEntity entity = jpaRepository.findById(establishment.getId())
                .orElse(toEntity(establishment));
        updateEntity(entity, establishment);
        EstablishmentEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Establishment> findById(UUID id) {
        return jpaRepository.findByIdAndActiveTrue(id).map(this::toDomain);
    }

    @Override
    public List<Establishment> findByOwnerId(String ownerId) {
        return jpaRepository.findByOwnerIdAndActiveTrue(ownerId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return jpaRepository.existsByCnpj(cnpj);
    }

    private void updateEntity(EstablishmentEntity entity, Establishment domain) {
        entity.setOwnerId(domain.getOwnerId());
        entity.setName(domain.getName());
        entity.setCnpj(domain.getCnpj());
        entity.setDescription(domain.getDescription());
        entity.setActive(domain.isActive());
        entity.setPhotos(domain.getPhotos());
        entity.setAddress(AddressEmbeddable.builder()
                .street(domain.getAddress().getStreet())
                .number(domain.getAddress().getNumber())
                .zipCode(domain.getAddress().getZipCode())
                .latitude(domain.getAddress().getLatitude())
                .longitude(domain.getAddress().getLongitude())
                .build());

        if (domain.getDefaultBusinessHours() != null) {
            entity.getDefaultBusinessHours().clear();
            entity.getDefaultBusinessHours().addAll(domain.getDefaultBusinessHours().stream()
                    .map(bh -> BusinessHourEntity.builder()
                            .id(UUID.randomUUID()) // Simple for now
                            .establishment(entity)
                            .dayOfWeek(bh.getDayOfWeek())
                            .openTime(bh.getOpenTime())
                            .closeTime(bh.getCloseTime())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (domain.getProvidedServices() != null) {
            entity.getProvidedServices().clear();
            entity.getProvidedServices().addAll(domain.getProvidedServices().stream()
                    .map(ps -> ProvidedServiceEntity.builder()
                            .id(ps.getId() != null ? ps.getId() : UUID.randomUUID())
                            .establishment(entity)
                            .title(ps.getTitle())
                            .description(ps.getDescription())
                            .active(ps.isActive())
                            .build())
                    .collect(Collectors.toList()));
        }
    }

    private EstablishmentEntity toEntity(Establishment domain) {
        EstablishmentEntity entity = EstablishmentEntity.builder()
                .id(domain.getId())
                .ownerId(domain.getOwnerId())
                .name(domain.getName())
                .cnpj(domain.getCnpj())
                .description(domain.getDescription())
                .active(domain.isActive())
                .photos(domain.getPhotos())
                .address(AddressEmbeddable.builder()
                        .street(domain.getAddress().getStreet())
                        .number(domain.getAddress().getNumber())
                        .zipCode(domain.getAddress().getZipCode())
                        .latitude(domain.getAddress().getLatitude())
                        .longitude(domain.getAddress().getLongitude())
                        .build())
                .build();

        if (domain.getDefaultBusinessHours() != null) {
            entity.setDefaultBusinessHours(domain.getDefaultBusinessHours().stream()
                    .map(bh -> BusinessHourEntity.builder()
                            .id(UUID.randomUUID())
                            .establishment(entity)
                            .dayOfWeek(bh.getDayOfWeek())
                            .openTime(bh.getOpenTime())
                            .closeTime(bh.getCloseTime())
                            .build())
                    .collect(Collectors.toList()));
        }

        if (domain.getProvidedServices() != null) {
            entity.setProvidedServices(domain.getProvidedServices().stream()
                    .map(ps -> ProvidedServiceEntity.builder()
                            .id(ps.getId() != null ? ps.getId() : UUID.randomUUID())
                            .establishment(entity)
                            .title(ps.getTitle())
                            .description(ps.getDescription())
                            .active(ps.isActive())
                            .build())
                    .collect(Collectors.toList()));
        }

        return entity;
    }

    private Establishment toDomain(EstablishmentEntity entity) {
        return Establishment.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .name(entity.getName())
                .cnpj(entity.getCnpj())
                .description(entity.getDescription())
                .active(entity.isActive())
                .photos(entity.getPhotos())
                .address(Address.builder()
                        .street(entity.getAddress().getStreet())
                        .number(entity.getAddress().getNumber())
                        .zipCode(entity.getAddress().getZipCode())
                        .latitude(entity.getAddress().getLatitude())
                        .longitude(entity.getAddress().getLongitude())
                        .build())
                .defaultBusinessHours(entity.getDefaultBusinessHours().stream()
                        .map(bh -> BusinessHour.builder()
                                .dayOfWeek(bh.getDayOfWeek())
                                .openTime(bh.getOpenTime())
                                .closeTime(bh.getCloseTime())
                                .build())
                        .collect(Collectors.toList()))
                .providedServices(entity.getProvidedServices().stream()
                        .map(ps -> ProvidedService.builder()
                                .id(ps.getId())
                                .title(ps.getTitle())
                                .description(ps.getDescription())
                                .active(ps.isActive())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
