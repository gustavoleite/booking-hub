package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tb_affiliations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AffiliationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "establishment_id")
    private EstablishmentEntity establishment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professional_id")
    private ProfessionalEntity professional;

    private boolean active;

    @OneToMany(mappedBy = "affiliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WorkScheduleEntity> workSchedules;

    @OneToMany(mappedBy = "affiliation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceOfferingEntity> serviceOfferings;
}
