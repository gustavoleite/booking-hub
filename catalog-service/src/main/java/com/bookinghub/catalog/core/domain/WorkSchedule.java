package com.bookinghub.catalog.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WorkSchedule {
    private final int dayOfWeek; // 1 to 7
    @JsonFormat(pattern = "HH:mm:ss")
    private final LocalTime startTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private final LocalTime endTime;

    public boolean overlaps(WorkSchedule other) {
        return this.dayOfWeek == other.dayOfWeek
                && !(this.endTime.isBefore(other.startTime) || this.endTime.equals(other.startTime)
                || this.startTime.isAfter(other.endTime) || this.startTime.equals(other.endTime));
    }
}
