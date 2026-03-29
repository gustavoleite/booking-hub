package com.bookinghub.catalog.infrastructure.adapters.out.database;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

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
    @CollectionTable(name = "tb_professional_specialties", joinColumns = @JoinColumn(name = "professional_id"))
    @Column(name = "specialty")
    private List<String> specialties;
}
