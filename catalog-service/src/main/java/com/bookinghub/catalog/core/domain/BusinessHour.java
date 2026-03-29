package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class BusinessHour {
    private final int dayOfWeek; // 1 (Monday) to 7 (Sunday)
    private final LocalTime openTime;
    private final LocalTime closeTime;

    public boolean isWithin(LocalTime start, LocalTime end) {
        return !start.isBefore(openTime) && !end.isAfter(closeTime);
    }
}
