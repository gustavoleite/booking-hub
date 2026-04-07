package com.bookinghub.catalog.infrastructure.configuration;

import com.bookinghub.catalog.core.ports.AffiliationRepository;
import com.bookinghub.catalog.core.ports.CatalogEventPublisher;
import com.bookinghub.catalog.core.ports.EstablishmentRepository;
import com.bookinghub.catalog.core.ports.ProfessionalRepository;
import com.bookinghub.catalog.core.usecases.AddProfessionalToEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.AddProvidedServiceUseCase;
import com.bookinghub.catalog.core.usecases.CreateEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.CreateProfessionalProfileUseCase;
import com.bookinghub.catalog.core.usecases.GetEstablishmentDetailsUseCase;
import com.bookinghub.catalog.core.usecases.GetProfessionalScheduleUseCase;
import com.bookinghub.catalog.core.usecases.InactivateEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.ListMyEstablishmentsUseCase;
import com.bookinghub.catalog.core.usecases.UpdateEstablishmentUseCase;
import com.bookinghub.catalog.core.usecases.UpdateProfessionalProfileUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Bean
  public CreateEstablishmentUseCase createEstablishmentUseCase(
      EstablishmentRepository repository, CatalogEventPublisher eventPublisher) {
    return new CreateEstablishmentUseCase(repository, eventPublisher);
  }

  @Bean
  public UpdateEstablishmentUseCase updateEstablishmentUseCase(
      EstablishmentRepository repository, CatalogEventPublisher eventPublisher) {
    return new UpdateEstablishmentUseCase(repository, eventPublisher);
  }

  @Bean
  public InactivateEstablishmentUseCase inactivateEstablishmentUseCase(
      EstablishmentRepository repository) {
    return new InactivateEstablishmentUseCase(repository);
  }

  @Bean
  public AddProvidedServiceUseCase addProvidedServiceUseCase(EstablishmentRepository repository) {
    return new AddProvidedServiceUseCase(repository);
  }

  @Bean
  public CreateProfessionalProfileUseCase createProfessionalProfileUseCase(
      ProfessionalRepository repository) {
    return new CreateProfessionalProfileUseCase(repository);
  }

  @Bean
  public UpdateProfessionalProfileUseCase updateProfessionalProfileUseCase(
      ProfessionalRepository repository) {
    return new UpdateProfessionalProfileUseCase(repository);
  }

  @Bean
  public GetEstablishmentDetailsUseCase getEstablishmentDetailsUseCase(
      EstablishmentRepository repository) {
    return new GetEstablishmentDetailsUseCase(repository);
  }

  @Bean
  public ListMyEstablishmentsUseCase listMyEstablishmentsUseCase(
      EstablishmentRepository repository) {
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
  public GetProfessionalScheduleUseCase getProfessionalScheduleUseCase(
      AffiliationRepository repository) {
    return new GetProfessionalScheduleUseCase(repository);
  }
}
