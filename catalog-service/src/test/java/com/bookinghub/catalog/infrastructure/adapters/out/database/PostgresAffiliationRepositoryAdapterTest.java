package com.bookinghub.catalog.infrastructure.adapters.out.database;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.ServiceOffering;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostgresAffiliationRepositoryAdapterTest {

  @Mock
  private JpaAffiliationRepository jpaRepository;

  @InjectMocks
  private PostgresAffiliationRepositoryAdapter adapter;

  @Test
  void shouldSaveAffiliation() {
    UUID id = UUID.randomUUID();
    Affiliation domain = Affiliation.builder()
        .id(id)
        .establishmentId(UUID.randomUUID())
        .professionalId(UUID.randomUUID())
        .workSchedules(List.of(WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.MIN).endTime(LocalTime.MAX).build()))
        .serviceOfferings(List.of(ServiceOffering.builder().providedServiceId(UUID.randomUUID()).price(BigDecimal.TEN).build()))
        .build();

    AffiliationEntity entity = AffiliationEntity.builder()
        .id(id)
        .establishment(EstablishmentEntity.builder().id(domain.getEstablishmentId()).build())
        .professional(ProfessionalEntity.builder().id(domain.getProfessionalId()).build())
        .workSchedules(new ArrayList<>())
        .serviceOfferings(new ArrayList<>())
        .build();
    when(jpaRepository.save(any(AffiliationEntity.class))).thenReturn(entity);

    Affiliation result = adapter.save(domain);

    assertNotNull(result);
    verify(jpaRepository).save(any(AffiliationEntity.class));
  }

  @Test
  void shouldFindByEstablishmentIdAndProfessionalId() {
    UUID estId = UUID.randomUUID();
    UUID profId = UUID.randomUUID();
    AffiliationEntity entity = AffiliationEntity.builder()
        .establishment(EstablishmentEntity.builder().id(estId).build())
        .professional(ProfessionalEntity.builder().id(profId).build())
        .workSchedules(new ArrayList<>())
        .serviceOfferings(new ArrayList<>())
        .build();
    when(jpaRepository.findByEstablishmentIdAndProfessionalId(estId, profId)).thenReturn(Optional.of(entity));

    Optional<Affiliation> result = adapter.findByEstablishmentIdAndProfessionalId(estId, profId);

    assertTrue(result.isPresent());
    assertEquals(estId, result.get().getEstablishmentId());
  }
}
