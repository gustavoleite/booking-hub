package com.bookinghub.search.infrastructure.configuration;

import com.bookinghub.search.core.ports.CatalogClient;
import com.bookinghub.search.core.ports.EstablishmentSearchRepository;
import com.bookinghub.search.core.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public IndexEstablishmentUseCase indexEstablishmentUseCase(EstablishmentSearchRepository repository) {
        return new IndexEstablishmentUseCase(repository);
    }

    @Bean
    public IndexAffiliationUseCase indexAffiliationUseCase(EstablishmentSearchRepository repository) {
        return new IndexAffiliationUseCase(repository);
    }

    @Bean
    public IndexReviewUseCase indexReviewUseCase(EstablishmentSearchRepository repository) {
        return new IndexReviewUseCase(repository);
    }

    @Bean
    public SearchEstablishmentsUseCase searchEstablishmentsUseCase(EstablishmentSearchRepository repository) {
        return new SearchEstablishmentsUseCase(repository);
    }

    @Bean
    public ReindexUseCase reindexUseCase(CatalogClient catalogClient, EstablishmentSearchRepository repository) {
        return new ReindexUseCase(catalogClient, repository);
    }
}
