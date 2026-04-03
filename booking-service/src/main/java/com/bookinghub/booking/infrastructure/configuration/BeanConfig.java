package com.bookinghub.booking.infrastructure.configuration;

import com.bookinghub.booking.core.ports.BookingEventPublisher;
import com.bookinghub.booking.core.ports.BookingRepository;
import com.bookinghub.booking.core.ports.CatalogServiceClient;
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
    public CompleteBookingUseCase completeBookingUseCase(BookingRepository bookingRepository,
                                                          BookingEventPublisher eventPublisher) {
        return new CompleteBookingUseCase(bookingRepository, eventPublisher);
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
