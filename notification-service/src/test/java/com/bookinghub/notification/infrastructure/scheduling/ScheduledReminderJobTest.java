package com.bookinghub.notification.infrastructure.scheduling;

import static org.mockito.Mockito.verify;

import com.bookinghub.notification.core.usecases.SendBookingReminderUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduledReminderJobTest {

    @Mock
    private SendBookingReminderUseCase sendBookingReminderUseCase;

    private ScheduledReminderJob job;

    @BeforeEach
    void setUp() {
        job = new ScheduledReminderJob(sendBookingReminderUseCase);
    }

    @Test
    void runHourly_shouldDelegateToUseCase() {
        job.runHourly();

        verify(sendBookingReminderUseCase).execute();
    }
}
