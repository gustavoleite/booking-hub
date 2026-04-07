package com.bookinghub.search.infrastructure.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(
        basePackages = "com.bookinghub.search.infrastructure.adapters.out.elasticsearch")
public class ElasticsearchConfig {
}
