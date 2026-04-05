package com.bookinghub.review.infrastructure.configuration;

import com.bookinghub.review.core.ports.EligibleBookingRepository;
import com.bookinghub.review.core.ports.ReviewEventPublisher;
import com.bookinghub.review.core.ports.ReviewRepository;
import com.bookinghub.review.core.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase(
            EligibleBookingRepository eligibleBookingRepository) {
        return new ConsumeBookingCompletedUseCase(eligibleBookingRepository);
    }

    @Bean
    public CreateReviewUseCase createReviewUseCase(ReviewRepository reviewRepository,
                                                    EligibleBookingRepository eligibleBookingRepository,
                                                    ReviewEventPublisher eventPublisher) {
        return new CreateReviewUseCase(reviewRepository, eligibleBookingRepository, eventPublisher);
    }

    @Bean
    public GetReviewsByProfessionalUseCase getReviewsByProfessionalUseCase(
            ReviewRepository reviewRepository) {
        return new GetReviewsByProfessionalUseCase(reviewRepository);
    }

    @Bean
    public GetReviewsByEstablishmentUseCase getReviewsByEstablishmentUseCase(
            ReviewRepository reviewRepository) {
        return new GetReviewsByEstablishmentUseCase(reviewRepository);
    }

    @Bean
    public GetReviewByBookingUseCase getReviewByBookingUseCase(ReviewRepository reviewRepository) {
        return new GetReviewByBookingUseCase(reviewRepository);
    }
}
