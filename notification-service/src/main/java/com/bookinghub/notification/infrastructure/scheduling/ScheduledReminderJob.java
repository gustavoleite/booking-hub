package com.bookinghub.notification.infrastructure.scheduling;

import com.bookinghub.notification.core.usecases.SendBookingReminderUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledReminderJob {

  private final SendBookingReminderUseCase sendBookingReminderUseCase;

  @Scheduled(cron = "0 0 * * * *")
  public void runHourly() {
    log.info("Running scheduled booking reminder job");
    sendBookingReminderUseCase.execute();
  }
}
