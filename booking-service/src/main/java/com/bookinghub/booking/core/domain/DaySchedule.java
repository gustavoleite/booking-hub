package com.bookinghub.booking.core.domain;

import java.time.LocalTime;

public record DaySchedule(int dayOfWeek, LocalTime startTime, LocalTime endTime) {
    public boolean contains(LocalTime time, int durationMinutes) {
        LocalTime slotEnd = time.plusMinutes(durationMinutes);
        return !time.isBefore(startTime) && !slotEnd.isAfter(endTime);
    }
}
