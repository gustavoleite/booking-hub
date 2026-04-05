package com.bookinghub.review.infrastructure.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String BOOKING_EXCHANGE = "booking.events";
    public static final String REVIEW_EXCHANGE = "review.events";
    public static final String REVIEW_BOOKING_COMPLETED_QUEUE = "review.booking.completed";

    public String getReviewBookingCompletedQueueName() {
        return REVIEW_BOOKING_COMPLETED_QUEUE;
    }

    @Bean
    public TopicExchange bookingExchange() {
        return ExchangeBuilder.topicExchange(BOOKING_EXCHANGE).durable(true).build();
    }

    @Bean
    public TopicExchange reviewExchange() {
        return ExchangeBuilder.topicExchange(REVIEW_EXCHANGE).durable(true).build();
    }

    @Bean
    public Queue reviewBookingCompletedQueue() {
        return QueueBuilder.durable(REVIEW_BOOKING_COMPLETED_QUEUE).build();
    }

    @Bean
    public Binding reviewBookingCompletedBinding(Queue reviewBookingCompletedQueue,
                                                  @Qualifier("bookingExchange") TopicExchange bookingExchange) {
        return BindingBuilder.bind(reviewBookingCompletedQueue)
                .to(bookingExchange)
                .with("booking.completed");
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
