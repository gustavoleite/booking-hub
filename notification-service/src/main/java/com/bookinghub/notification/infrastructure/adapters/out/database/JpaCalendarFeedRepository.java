package com.bookinghub.notification.infrastructure.adapters.out.database;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCalendarFeedRepository
        extends JpaRepository<CalendarFeedEntity, UUID> {

    Optional<CalendarFeedEntity> findByUserId(String userId);

    Optional<CalendarFeedEntity> findByUserIdAndFeedToken(String userId, String feedToken);
}
