package com.bookinghub.catalog.core.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.catalog.core.domain.Affiliation;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.Professional;
import com.bookinghub.catalog.core.domain.WorkSchedule;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AddProfessionalToEstablishmentUseCaseTest {

  @Mock
  private EstablishmentRepository establishmentRepository;
  @Mock
  private ProfessionalRepository professionalRepository;
  @Mock
  private AffiliationRepository affiliationRepository;
  @Mock
  private CatalogEventPublisher eventPublisher;

  @InjectMocks
  private AddProfessionalToEstablishmentUseCase useCase;

  private UUID establishmentId;
  private UUID professionalId;
  private Establishment establishment;
  private Professional professional;

  @BeforeEach
  void setUp() {
    establishmentId = UUID.randomUUID();
    professionalId = UUID.randomUUID();

    establishment = Establishment.builder()
        .id(establishmentId)
        .defaultBusinessHours(List.of(
            BusinessHour.builder()
                .dayOfWeek(1)
                .openTime(LocalTime.of(8, 0))
                .closeTime(LocalTime.of(18, 0))
                .build()
        ))
        .build();

    professional = Professional.builder()
        .id(professionalId)
        .name("Dr. Test")
        .specialties(List.of("Haircut", "Styling"))
        .build();
  }

  @Test
  void shouldAddProfessionalToEstablishment() {
    Affiliation affiliation = Affiliation.builder()
        .establishmentId(establishmentId)
        .professionalId(professionalId)
        .workSchedules(List.of(
            WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build()
        ))
        .build();

    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
    when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
    when(affiliationRepository.findByEstablishmentIdAndProfessionalId(establishmentId, professionalId)).thenReturn(Optional.empty());
    when(affiliationRepository.save(any(Affiliation.class))).thenReturn(affiliation);

    Affiliation result = useCase.execute(establishmentId, professionalId, affiliation);

    assertNotNull(result);
    verify(affiliationRepository).save(affiliation);
    verify(eventPublisher).publishAffiliationCreated(eq(result), any(Professional.class), any(Establishment.class));
  }

  @Test
  void shouldThrowExceptionWhenEstablishmentNotFound() {
    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> useCase.execute(establishmentId, professionalId, Affiliation.builder().build()));

    assertTrue(exception.getMessage().contains("Estabelecimento não encontrado"));
  }

  @Test
  void shouldThrowExceptionWhenProfessionalNotFound() {
    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
    when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> useCase.execute(establishmentId, professionalId, Affiliation.builder().build()));

    assertTrue(exception.getMessage().contains("Profissional não encontrado"));
  }

  @Test
  void shouldThrowExceptionWhenSalonIsClosedOnDay() {
    Affiliation affiliation = Affiliation.builder()
        .workSchedules(List.of(
            WorkSchedule.builder().dayOfWeek(2).startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build()
        ))
        .build();

    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
    when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> useCase.execute(establishmentId, professionalId, affiliation));

    assertTrue(exception.getMessage().contains("O salão não funciona no dia 2"));
  }

  @Test
  void shouldThrowExceptionWhenOutsideSalonHours() {
    Affiliation affiliation = Affiliation.builder()
        .workSchedules(List.of(
            WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(7, 0)).endTime(LocalTime.of(17, 0)).build()
        ))
        .build();

    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
    when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

    RuntimeException exception = assertThrows(RuntimeException.class,
        () -> useCase.execute(establishmentId, professionalId, affiliation));

    assertEquals("Horário do profissional fora do expediente do salão", exception.getMessage());
  }

  @Test
  void shouldPublishAffiliationUpdatedWhenAffiliationAlreadyExists() {
    UUID existingAffiliationId = UUID.randomUUID();
    Affiliation existingAffiliation = Affiliation.builder()
        .id(existingAffiliationId)
        .establishmentId(establishmentId)
        .professionalId(professionalId)
        .active(true)
        .build();

    Affiliation incomingAffiliation = Affiliation.builder()
        .establishmentId(establishmentId)
        .professionalId(professionalId)
        .active(true)
        .workSchedules(List.of(
            WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(17, 0))
                .build()
        ))
        .build();

    when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
    when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
    when(affiliationRepository.findByEstablishmentIdAndProfessionalId(establishmentId, professionalId))
        .thenReturn(Optional.of(existingAffiliation));
    when(affiliationRepository.save(any(Affiliation.class))).thenReturn(incomingAffiliation);

    Affiliation result = useCase.execute(establishmentId, professionalId, incomingAffiliation);

    assertNotNull(result);
    verify(eventPublisher).publishAffiliationUpdated(eq(result), any(Professional.class), any(Establishment.class));
    verify(eventPublisher, never()).publishAffiliationCreated(any(), any(), any());
  }
}
