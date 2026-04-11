package com.bookinghub.notification.core.usecases;

import com.bookinghub.notification.core.domain.BookingSnapshot;
import com.bookinghub.notification.core.ports.BookingSnapshotRepository;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import com.bookinghub.notification.infrastructure.adapters.out.ical.ICalendarGenerator;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateCalendarFeedUseCase {

    private final CalendarFeedRepository feedRepository;
    private final BookingSnapshotRepository snapshotRepository;
    private final ICalendarGenerator generator;

    public GenerateCalendarFeedUseCase(
      CalendarFeedRepository feedRepository,
      BookingSnapshotRepository snapshotRepository,
      ICalendarGenerator generator) {
        this.feedRepository = feedRepository;
        this.snapshotRepository = snapshotRepository;
        this.generator = generator;
    }

    public String execute(String userId, String feedToken) {
        feedRepository.findByUserIdAndFeedToken(userId, feedToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid feed token"));

        List<BookingSnapshot> snapshots = new ArrayList<>();
        snapshots.addAll(snapshotRepository.findByClientId(userId));

        try {
            UUID professionalId = UUID.fromString(userId);
            snapshots.addAll(snapshotRepository.findByProfessionalId(professionalId));
        } catch (IllegalArgumentException ignored) {
            // userId is not a UUID — user is a client only, no professional bookings to add
        }

        return generator.generate(snapshots);
    }
}
