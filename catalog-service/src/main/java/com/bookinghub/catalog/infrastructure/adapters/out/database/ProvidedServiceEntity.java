package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

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
