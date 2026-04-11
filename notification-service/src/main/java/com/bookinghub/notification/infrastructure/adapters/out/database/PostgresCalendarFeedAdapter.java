package com.bookinghub.notification.infrastructure.adapters.out.database;

import com.bookinghub.notification.core.domain.CalendarFeed;
import com.bookinghub.notification.core.ports.CalendarFeedRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostgresCalendarFeedAdapter implements CalendarFeedRepository {

    private final JpaCalendarFeedRepository jpa;

    @Override
    public void save(CalendarFeed feed) {
        jpa.save(toEntity(feed));
    }

    @Override
    public Optional<CalendarFeed> findByUserId(String userId) {
        return jpa.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public Optional<CalendarFeed> findByUserIdAndFeedToken(String userId, String feedToken) {
        return jpa.findByUserIdAndFeedToken(userId, feedToken).map(this::toDomain);
    }

    private CalendarFeedEntity toEntity(CalendarFeed f) {
        return CalendarFeedEntity.builder()
                .id(f.getId())
                .userId(f.getUserId())
                .feedToken(f.getFeedToken())
                .createdAt(f.getCreatedAt())
                .build();
    }

    private CalendarFeed toDomain(CalendarFeedEntity e) {
        return CalendarFeed.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .feedToken(e.getFeedToken())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
