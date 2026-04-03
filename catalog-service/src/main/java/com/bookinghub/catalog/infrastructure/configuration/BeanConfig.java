package com.bookinghub.catalog.infrastructure.configuration;

import com.bookinghub.catalog.core.ports.*;
import com.bookinghub.catalog.core.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CreateEstablishmentUseCase createEstablishmentUseCase(EstablishmentRepository repository) {
        return new CreateEstablishmentUseCase(repository);
    }

    @Bean
    public UpdateEstablishmentUseCase updateEstablishmentUseCase(EstablishmentRepository repository) {
        return new UpdateEstablishmentUseCase(repository);
    }

    @Bean
    public InactivateEstablishmentUseCase inactivateEstablishmentUseCase(EstablishmentRepository repository) {
        return new InactivateEstablishmentUseCase(repository);
    }

    @Bean
    public AddProvidedServiceUseCase addProvidedServiceUseCase(EstablishmentRepository repository) {
        return new AddProvidedServiceUseCase(repository);
    }

    @Bean
    public CreateProfessionalProfileUseCase createProfessionalProfileUseCase(ProfessionalRepository repository) {
        return new CreateProfessionalProfileUseCase(repository);
    }

    @Bean
    public UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase(ProfessionalRepository repository) {
        return new UpdateProfessionalProfileUseCase(repository);
    }

    @Bean
    public GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase(EstablishmentRepository repository) {
        return new GetEstablishmentDetailsUseCase(repository);
    }

    @Bean
    public ListMyEstablishmentsUseCase listMyEstablishmentsUseCase(EstablishmentRepository repository) {
        return new ListMyEstablishmentsUseCase(repository);
    }

    @Bean
    public AddProfessionalToEstablishmentUseCase addProfessionalToEstablishmentUseCase(
            EstablishmentRepository establishmentRepository,
            ProfessionalRepository professionalRepository,
            AffiliationRepository affiliationRepository,
            CatalogEventPublisher eventPublisher) {
        return new AddProfessionalToEstablishmentUseCase(
                establishmentRepository,
                professionalRepository,
                affiliationRepository,
                eventPublisher
        );
    }

    @Bean
    public GetProfessionalScheduleUseCase getProfessionalScheduleUseCase(AffiliationRepository repository) {
        return new GetProfessionalScheduleUseCase(repository);
    }
}
