package com.bookinghub.catalog.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AffiliationTest {

  @Test
  void shouldActivateAndDeactivate() {
    Affiliation affiliation = Affiliation.builder()
        .active(false)
        .build();

    affiliation.activate();
    assertTrue(affiliation.isActive());

    affiliation.deactivate();
    assertFalse(affiliation.isActive());
  }

  @Test
  void shouldUpdateSchedules() {
    Affiliation affiliation = Affiliation.builder().build();
    List<WorkSchedule> schedules = List.of(
        WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build(),
        WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(17, 0)).build()
    );

    affiliation.updateSchedules(schedules);

    assertEquals(schedules, affiliation.getWorkSchedules());
  }

  @Test
  void shouldThrowExceptionWhenSchedulesOverlap() {
    Affiliation affiliation = Affiliation.builder().build();
    List<WorkSchedule> schedules = List.of(
        WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build(),
        WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(11, 0)).endTime(LocalTime.of(13, 0)).build()
    );

    RuntimeException exception = assertThrows(RuntimeException.class, () -> affiliation.updateSchedules(schedules));
    assertTrue(exception.getMessage().contains("sobrepostos") || exception.getMessage().contains("Overlapping"));
  }

  @Test
  void shouldHaveGetters() {
    UUID id = UUID.randomUUID();
    UUID estId = UUID.randomUUID();
    UUID profId = UUID.randomUUID();
    List<ServiceOffering> offerings = List.of();

    Affiliation affiliation = Affiliation.builder()
        .id(id)
        .establishmentId(estId)
        .professionalId(profId)
        .serviceOfferings(offerings)
        .build();

    assertEquals(id, affiliation.getId());
    assertEquals(estId, affiliation.getEstablishmentId());
    assertEquals(profId, affiliation.getProfessionalId());
    assertEquals(offerings, affiliation.getServiceOfferings());
  }
}
