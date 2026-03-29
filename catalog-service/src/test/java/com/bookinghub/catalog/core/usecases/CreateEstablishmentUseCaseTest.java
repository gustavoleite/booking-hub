package com.bookinghub.catalog.core.usecases;

import com.bookinghub.catalog.core.domain.Address;
import com.bookinghub.catalog.core.domain.BusinessHour;
import com.bookinghub.catalog.core.domain.Establishment;
import com.bookinghub.catalog.core.domain.ProvidedService;
import com.bookinghub.catalog.core.exceptions.BusinessRuleException;
import com.bookinghub.catalog.core.exceptions.ConflictException;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateEstablishmentUseCaseTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    @InjectMocks
    private CreateEstablishmentUseCase createEstablishmentUseCase;

    private Establishment validEstablishment;

    @BeforeEach
    void setUp() {
        validEstablishment = Establishment.builder()
                .ownerId("owner-1")
                .name("Test Salon")
                .cnpj("12345678000199")
                .address(Address.builder().street("Main St").build())
                .providedServices(List.of(ProvidedService.builder().title("Cut").build()))
                .defaultBusinessHours(List.of(
                        BusinessHour.builder()
                                .dayOfWeek(1)
                                .openTime(LocalTime.of(9, 0))
                                .closeTime(LocalTime.of(18, 0))
                                .build()
                ))
                .build();
    }

    @Test
    void shouldCreateEstablishmentWhenValid() {
        when(establishmentRepository.existsByCnpj(any())).thenReturn(false);
        when(establishmentRepository.save(any())).thenReturn(validEstablishment);

        createEstablishmentUseCase.execute(validEstablishment);

        verify(establishmentRepository).save(any());
    }

    @Test
    void shouldThrowExceptionWhenCnpjAlreadyExists() {
        when(establishmentRepository.existsByCnpj(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> createEstablishmentUseCase.execute(validEstablishment));
    }

    @Test
    void shouldThrowExceptionWhenCnpjIsInvalid() {
        validEstablishment = Establishment.builder()
                .cnpj("123")
                .address(Address.builder().street("Main St").build())
                .build();

        assertThrows(BusinessRuleException.class, () -> createEstablishmentUseCase.execute(validEstablishment));
    }

    @Test
    void shouldThrowExceptionWhenAddressIsMissing() {
        validEstablishment = Establishment.builder()
                .cnpj("12345678000199")
                .build();

        assertThrows(BusinessRuleException.class, () -> createEstablishmentUseCase.execute(validEstablishment));
    }

    @Test
    void shouldThrowExceptionWhenNoServicesProvided() {
        validEstablishment = Establishment.builder()
                .cnpj("12345678000199")
                .address(Address.builder().street("Main St").build())
                .providedServices(Collections.emptyList())
                .build();

        assertThrows(BusinessRuleException.class, () -> createEstablishmentUseCase.execute(validEstablishment));
    }

    @Test
    void shouldThrowExceptionWhenBusinessHoursAreInvalid() {
        validEstablishment = Establishment.builder()
                .cnpj("12345678000199")
                .address(Address.builder().street("Main St").build())
                .providedServices(List.of(ProvidedService.builder().title("Cut").build()))
                .defaultBusinessHours(List.of(
                        BusinessHour.builder()
                                .dayOfWeek(1)
                                .openTime(LocalTime.of(18, 0))
                                .closeTime(LocalTime.of(9, 0))
                                .build()
                ))
                .build();

        assertThrows(BusinessRuleException.class, () -> createEstablishmentUseCase.execute(validEstablishment));
    }
}
