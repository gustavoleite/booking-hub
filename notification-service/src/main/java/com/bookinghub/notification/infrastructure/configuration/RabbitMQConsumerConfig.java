package com.bookinghub.notification.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConsumerConfig {

  public static final String BOOKING_EXCHANGE = "booking.events";
  public static final String SYNC_QUEUE = "calendar.sync.queue";
  public static final String DLX = "calendar.dlx";
  public static final String DLQ = "calendar.sync.dlq";

  @Bean
  public TopicExchange bookingEventsExchange() {
    return ExchangeBuilder.topicExchange(BOOKING_EXCHANGE).durable(true).build();
  }

  @Bean
  public DirectExchange calendarDeadLetterExchange() {
    return ExchangeBuilder.directExchange(DLX).durable(true).build();
  }

  @Bean
  public Queue calendarSyncQueue() {
    return QueueBuilder.durable(SYNC_QUEUE)
        .deadLetterExchange(DLX)
        .deadLetterRoutingKey(DLQ)
        .build();
  }

  @Bean
  public Queue calendarDeadLetterQueue() {
    return QueueBuilder.durable(DLQ).build();
  }

  @Bean
  public Binding dlqBinding(Queue calendarDeadLetterQueue,
      DirectExchange calendarDeadLetterExchange) {
    return BindingBuilder.bind(calendarDeadLetterQueue)
        .to(calendarDeadLetterExchange)
        .with(DLQ);
  }

  @Bean
  public Binding bindingCreated(Queue calendarSyncQueue, TopicExchange bookingEventsExchange) {
    return BindingBuilder.bind(calendarSyncQueue).to(bookingEventsExchange).with("booking.created");
  }

  @Bean
  public Binding bindingCancelled(Queue calendarSyncQueue, TopicExchange bookingEventsExchange) {
    return BindingBuilder.bind(calendarSyncQueue)
        .to(bookingEventsExchange).with("booking.cancelled");
  }

  @Bean
  public Binding bindingCompleted(Queue calendarSyncQueue, TopicExchange bookingEventsExchange) {
    return BindingBuilder.bind(calendarSyncQueue)
        .to(bookingEventsExchange).with("booking.completed");
  }

  @Bean
  public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
    return new Jackson2JsonMessageConverter(objectMapper);
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory,
      Jackson2JsonMessageConverter messageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(messageConverter);
    return factory;
  }
}
