package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

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
    @CollectionTable(name = "tb_establishment_photos", joinColumns = @JoinColumn(name = "establishment_id"))
    @Column(name = "photo_url")
    private List<String> photos;

    @Embedded
    private AddressEmbeddable address;

    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<BusinessHourEntity> defaultBusinessHours;

    @OneToMany(mappedBy = "establishment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProvidedServiceEntity> providedServices;
}
