package com.bookinghub.search.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String CATALOG_EXCHANGE = "catalog.events";
    public static final String REVIEW_EXCHANGE = "review.events";

    public static final String SEARCH_ESTABLISHMENT_CREATED_QUEUE = "search.establishment.created";
    public static final String SEARCH_ESTABLISHMENT_UPDATED_QUEUE = "search.establishment.updated";
    public static final String SEARCH_AFFILIATION_CREATED_QUEUE = "search.affiliation.created";
    public static final String SEARCH_AFFILIATION_UPDATED_QUEUE = "search.affiliation.updated";
    public static final String SEARCH_REVIEW_CREATED_QUEUE = "search.review.created";

    public String getSearchEstablishmentCreatedQueueName() { return SEARCH_ESTABLISHMENT_CREATED_QUEUE; }
    public String getSearchEstablishmentUpdatedQueueName() { return SEARCH_ESTABLISHMENT_UPDATED_QUEUE; }
    public String getSearchAffiliationCreatedQueueName() { return SEARCH_AFFILIATION_CREATED_QUEUE; }
    public String getSearchAffiliationUpdatedQueueName() { return SEARCH_AFFILIATION_UPDATED_QUEUE; }
    public String getSearchReviewCreatedQueueName() { return SEARCH_REVIEW_CREATED_QUEUE; }

    @Bean
    public TopicExchange catalogExchange() {
        return ExchangeBuilder.topicExchange(CATALOG_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange reviewExchange() {
        return ExchangeBuilder.topicExchange(REVIEW_EXCHANGE).durable(true).build();
    }

    @Bean public Queue searchEstablishmentCreatedQueue() { return QueueBuilder.durable(SEARCH_ESTABLISHMENT_CREATED_QUEUE).build(); }
    @Bean public Queue searchEstablishmentUpdatedQueue() { return QueueBuilder.durable(SEARCH_ESTABLISHMENT_UPDATED_QUEUE).build(); }
    @Bean public Queue searchAffiliationCreatedQueue() { return QueueBuilder.durable(SEARCH_AFFILIATION_CREATED_QUEUE).build(); }
    @Bean public Queue searchAffiliationUpdatedQueue() { return QueueBuilder.durable(SEARCH_AFFILIATION_UPDATED_QUEUE).build(); }
    @Bean public Queue searchReviewCreatedQueue() { return QueueBuilder.durable(SEARCH_REVIEW_CREATED_QUEUE).build(); }

    @Bean
    public Binding searchEstablishmentCreatedBinding(
            @Qualifier("searchEstablishmentCreatedQueue") Queue searchEstablishmentCreatedQueue,
            @Qualifier("catalogExchange") TopicExchange catalogExchange) {
        return BindingBuilder.bind(searchEstablishmentCreatedQueue).to(catalogExchange).with("establishment.created");
    }

    @Bean
    public Binding searchEstablishmentUpdatedBinding(
            @Qualifier("searchEstablishmentUpdatedQueue") Queue searchEstablishmentUpdatedQueue,
            @Qualifier("catalogExchange") TopicExchange catalogExchange) {
        return BindingBuilder.bind(searchEstablishmentUpdatedQueue).to(catalogExchange).with("establishment.updated");
    }

    @Bean
    public Binding searchAffiliationCreatedBinding(
            @Qualifier("searchAffiliationCreatedQueue") Queue searchAffiliationCreatedQueue,
            @Qualifier("catalogExchange") TopicExchange catalogExchange) {
        return BindingBuilder.bind(searchAffiliationCreatedQueue).to(catalogExchange).with("affiliation.created");
    }

    @Bean
    public Binding searchAffiliationUpdatedBinding(
            @Qualifier("searchAffiliationUpdatedQueue") Queue searchAffiliationUpdatedQueue,
            @Qualifier("catalogExchange") TopicExchange catalogExchange) {
        return BindingBuilder.bind(searchAffiliationUpdatedQueue).to(catalogExchange).with("affiliation.updated");
    }

    @Bean
    public Binding searchReviewCreatedBinding(
            @Qualifier("searchReviewCreatedQueue") Queue searchReviewCreatedQueue,
            @Qualifier("reviewExchange") TopicExchange reviewExchange) {
        return BindingBuilder.bind(searchReviewCreatedQueue).to(reviewExchange).with("review.created");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
