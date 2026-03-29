package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class WorkSchedule {
    private final int dayOfWeek; // 1 to 7
    private final LocalTime startTime;
    private final LocalTime endTime;

    public boolean overlaps(WorkSchedule other) {
        if (this.dayOfWeek != other.dayOfWeek) {
            return false;
        }
        return !(this.endTime.isBefore(other.startTime) || this.endTime.equals(other.startTime) ||
                 this.startTime.isAfter(other.endTime) || this.startTime.equals(other.endTime));
    }
}
