package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.*;
import com.bookinghub.catalog.core.ports.AffiliationRepository;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
        when(affiliationRepository.save(any(Affiliation.class))).thenReturn(affiliation);

        Affiliation result = useCase.execute(establishmentId, professionalId, affiliation);

        assertNotNull(result);
        verify(affiliationRepository).save(affiliation);
        verify(eventPublisher).publishAffiliationCreated(result);
    }

    @Test
    void shouldThrowExceptionWhenEstablishmentNotFound() {
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> useCase.execute(establishmentId, professionalId, Affiliation.builder().build()));
        
        assertEquals("Establishment not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenProfessionalNotFound() {
        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> useCase.execute(establishmentId, professionalId, Affiliation.builder().build()));
        
        assertEquals("Professional not found", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenSalonIsClosedOnDay() {
        Affiliation affiliation = Affiliation.builder()
                .workSchedules(List.of(
                        WorkSchedule.builder().dayOfWeek(2).startTime(LocalTime.of(9,0)).endTime(LocalTime.of(17,0)).build()
                ))
                .build();

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> useCase.execute(establishmentId, professionalId, affiliation));
        
        assertEquals("Salon is closed on day 2", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenOutsideSalonHours() {
        Affiliation affiliation = Affiliation.builder()
                .workSchedules(List.of(
                        WorkSchedule.builder().dayOfWeek(1).startTime(LocalTime.of(7,0)).endTime(LocalTime.of(17,0)).build()
                ))
                .build();

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        RuntimeException exception = assertThrows(RuntimeException.class, 
                () -> useCase.execute(establishmentId, professionalId, affiliation));
        
        assertEquals("Horário do profissional fora do expediente do salão", exception.getMessage());
    }
}
