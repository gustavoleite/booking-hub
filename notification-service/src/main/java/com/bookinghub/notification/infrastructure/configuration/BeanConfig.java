package com.bookinghub.notification.infrastructure.configuration;

import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import com.bookinghub.notification.core.usecases.GenerateCalendarFeedUseCase;
import com.bookinghub.notification.core.usecases.GetOrCreateFeedTokenUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCancelledUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCreatedUseCase;
import com.bookinghub.notification.infrastructure.adapters.out.ical.ICalendarGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Value("${notification.base-url}")
  private String baseUrl;

  @Bean
  public HandleBookingCreatedUseCase handleBookingCreatedUseCase(
      BookingSnapshotRepository repository) {
    return new HandleBookingCreatedUseCase(repository);
  }

  @Bean
  public HandleBookingCancelledUseCase handleBookingCancelledUseCase(
      BookingSnapshotRepository repository) {
    return new HandleBookingCancelledUseCase(repository);
  }

  @Bean
  public HandleBookingCompletedUseCase handleBookingCompletedUseCase(
      BookingSnapshotRepository repository) {
    return new HandleBookingCompletedUseCase(repository);
  }

  @Bean
  public GetOrCreateFeedTokenUseCase getOrCreateFeedTokenUseCase(
      CalendarFeedRepository repository) {
    return new GetOrCreateFeedTokenUseCase(repository, baseUrl);
  }

  @Bean
  public GenerateCalendarFeedUseCase generateCalendarFeedUseCase(
      CalendarFeedRepository feedRepository,
      BookingSnapshotRepository snapshotRepository,
      ICalendarGenerator generator) {
    return new GenerateCalendarFeedUseCase(feedRepository, snapshotRepository, generator);
  }
}
