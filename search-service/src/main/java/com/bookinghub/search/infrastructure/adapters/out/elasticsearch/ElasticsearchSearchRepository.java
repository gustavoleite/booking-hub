package com.bookinghub.search.infrastructure.adapters.out.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ElasticsearchSearchRepository
        extends ElasticsearchRepository<EstablishmentEsDocument, String> {
}
