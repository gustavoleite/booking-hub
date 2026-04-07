package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_establishments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstablishmentEntity {
    @Id
    private UUID id;
    private String ownerId;
    private String name;
    private String cnpj;
    private String description;
    @Builder.Default
    private boolean active = true;

    @ElementCollection
    @CollectionTable(
            name = "tb_establishment_photos", joinColumns = @JoinColumn(name = "establishment_id"))
    @Column(name = "photo_url")
    private List<String> photos;

    @Embedded
    private AddressEmbeddable address;

    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BusinessHourEntity> defaultBusinessHours;

    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProvidedServiceEntity> providedServices;
}
