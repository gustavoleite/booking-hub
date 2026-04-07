package com.bookinghub.catalog.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

  private static final String EXCHANGE = "catalog.events";

  @Bean
  public TopicExchange catalogExchange() {
    return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public RabbitTemplate rabbitTemplate(
      ConnectionFactory connectionFactory, Jackson2JsonMessageConverter messageConverter) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(messageConverter);
    return rabbitTemplate;
  }
}
