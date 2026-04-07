package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
