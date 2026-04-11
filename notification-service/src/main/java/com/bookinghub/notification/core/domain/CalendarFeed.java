package com.bookinghub.notification.core.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CalendarFeed {

    private final UUID id;
    private final String userId;
    private final String feedToken;
    private final LocalDateTime createdAt;

    public static CalendarFeed create(String userId) {
        return CalendarFeed.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .feedToken(UUID.randomUUID().toString().replace("-", ""))
                .createdAt(LocalDateTime.now())
                .build();
    }
}
