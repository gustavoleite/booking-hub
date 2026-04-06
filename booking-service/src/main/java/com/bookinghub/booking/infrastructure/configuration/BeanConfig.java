package com.bookinghub.booking.infrastructure.configuration;

import com.bookinghub.booking.core.ports.*;
import com.bookinghub.booking.core.usecases.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public CreateBookingUseCase createBookingUseCase(BookingRepository bookingRepository,
                                                     CatalogServiceClient catalogServiceClient,
                                                     BookingEventPublisher eventPublisher) {
        return new CreateBookingUseCase(bookingRepository, catalogServiceClient, eventPublisher);
    }

    @Bean
    public GetAvailableSlotsUseCase getAvailableSlotsUseCase(BookingRepository bookingRepository,
                                                              CatalogServiceClient catalogServiceClient) {
        return new GetAvailableSlotsUseCase(bookingRepository, catalogServiceClient);
    }

    @Bean
    public CancelBookingUseCase cancelBookingUseCase(BookingRepository bookingRepository,
                                                     BookingEventPublisher eventPublisher) {
        return new CancelBookingUseCase(bookingRepository, eventPublisher);
    }

    @Bean
    public ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase(
            EligibleBookingRepository eligibleBookingRepository) {
        return new ConsumeBookingCompletedUseCase(eligibleBookingRepository);
    }

    @Bean
    public CompleteBookingUseCase completeBookingUseCase(BookingRepository bookingRepository,
                                                          BookingEventPublisher eventPublisher,
                                                          ConsumeBookingCompletedUseCase consumeBookingCompletedUseCase) {
        return new CompleteBookingUseCase(bookingRepository, eventPublisher, consumeBookingCompletedUseCase);
    }

    @Bean
    public CreateReviewUseCase createReviewUseCase(ReviewRepository reviewRepository,
                                                    EligibleBookingRepository eligibleBookingRepository,
                                                    ReviewEventPublisher reviewEventPublisher) {
        return new CreateReviewUseCase(reviewRepository, eligibleBookingRepository, reviewEventPublisher);
    }

    @Bean
    public GetReviewsByProfessionalUseCase getReviewsByProfessionalUseCase(ReviewRepository reviewRepository) {
        return new GetReviewsByProfessionalUseCase(reviewRepository);
    }

    @Bean
    public GetReviewsByEstablishmentUseCase getReviewsByEstablishmentUseCase(ReviewRepository reviewRepository) {
        return new GetReviewsByEstablishmentUseCase(reviewRepository);
    }

    @Bean
    public GetReviewByBookingUseCase getReviewByBookingUseCase(ReviewRepository reviewRepository) {
        return new GetReviewByBookingUseCase(reviewRepository);
    }

    @Bean
    public MarkNoShowUseCase markNoShowUseCase(BookingRepository bookingRepository) {
        return new MarkNoShowUseCase(bookingRepository);
    }

    @Bean
    public ListClientBookingsUseCase listClientBookingsUseCase(BookingRepository bookingRepository) {
        return new ListClientBookingsUseCase(bookingRepository);
    }

    @Bean
    public ListProfessionalAgendaUseCase listProfessionalAgendaUseCase(BookingRepository bookingRepository) {
        return new ListProfessionalAgendaUseCase(bookingRepository);
    }

    @Bean
    public ListEstablishmentBookingsUseCase listEstablishmentBookingsUseCase(BookingRepository bookingRepository) {
        return new ListEstablishmentBookingsUseCase(bookingRepository);
    }

    @Bean
    public GetBookingDetailsUseCase getBookingDetailsUseCase(BookingRepository bookingRepository) {
        return new GetBookingDetailsUseCase(bookingRepository);
    }
}
