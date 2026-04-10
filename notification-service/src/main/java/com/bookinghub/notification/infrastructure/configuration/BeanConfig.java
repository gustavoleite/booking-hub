package com.bookinghub.notification.infrastructure.configuration;

import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import com.bookinghub.notification.core.ports.EmailPort;
import com.bookinghub.notification.core.usecases.GenerateCalendarFeedUseCase;
import com.bookinghub.notification.core.usecases.GetOrCreateFeedTokenUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCancelledUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.HandleBookingCreatedUseCase;
import com.bookinghub.notification.core.usecases.SendBookingCancellationUseCase;
import com.bookinghub.notification.core.usecases.SendBookingCompletedUseCase;
import com.bookinghub.notification.core.usecases.SendBookingConfirmationUseCase;
import com.bookinghub.notification.core.usecases.SendBookingReminderUseCase;
import com.bookinghub.notification.infrastructure.adapters.out.ical.ICalendarGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Value("${notification.base-url}")
  private String baseUrl;

  @Bean
  public SendBookingConfirmationUseCase sendBookingConfirmationUseCase(EmailPort emailPort) {
    return new SendBookingConfirmationUseCase(emailPort);
  }

  @Bean
  public SendBookingCancellationUseCase sendBookingCancellationUseCase(EmailPort emailPort) {
    return new SendBookingCancellationUseCase(emailPort);
  }

  @Bean
  public SendBookingCompletedUseCase sendBookingCompletedUseCase(EmailPort emailPort) {
    return new SendBookingCompletedUseCase(emailPort);
  }

  @Bean
  public SendBookingReminderUseCase sendBookingReminderUseCase(
      BookingSnapshotRepository repository, EmailPort emailPort) {
    return new SendBookingReminderUseCase(repository, emailPort);
  }

  @Bean
  public HandleBookingCreatedUseCase handleBookingCreatedUseCase(
      BookingSnapshotRepository repository,
      SendBookingConfirmationUseCase sendConfirmation) {
    return new HandleBookingCreatedUseCase(repository, sendConfirmation);
  }

  @Bean
  public HandleBookingCancelledUseCase handleBookingCancelledUseCase(
      BookingSnapshotRepository repository,
      SendBookingCancellationUseCase sendCancellation) {
    return new HandleBookingCancelledUseCase(repository, sendCancellation);
  }

  @Bean
  public HandleBookingCompletedUseCase handleBookingCompletedUseCase(
      BookingSnapshotRepository repository,
      SendBookingCompletedUseCase sendCompleted) {
    return new HandleBookingCompletedUseCase(repository, sendCompleted);
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
