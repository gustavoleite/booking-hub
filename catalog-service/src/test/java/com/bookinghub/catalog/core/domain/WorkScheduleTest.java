package com.bookinghub.catalog.core.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class WorkScheduleTest {

    @Test
    void shouldOverlapSameDay() {
        WorkSchedule s1 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        WorkSchedule s2 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        assertTrue(s1.overlaps(s2));
        assertTrue(s2.overlaps(s1));
    }

    @Test
    void shouldNotOverlapDifferentDay() {
        WorkSchedule s1 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        WorkSchedule s2 = WorkSchedule.builder()
                .dayOfWeek(2)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        assertFalse(s1.overlaps(s2));
    }

    @Test
    void shouldNotOverlapSameDayAdjacent() {
        WorkSchedule s1 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        WorkSchedule s2 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(13, 0))
                .build();

        // Looking at the implementation: 
        // return !(this.endTime.isBefore(other.startTime) || this.endTime.equals(other.startTime) ||
        //          this.startTime.isAfter(other.endTime) || this.startTime.equals(other.endTime));
        // If s1.endTime (12:00) equals s2.startTime (12:00), it returns !(true || ...) which is false.
        // So they don't overlap if they are adjacent. Correct.
        assertFalse(s1.overlaps(s2));
        assertFalse(s2.overlaps(s1));
    }

    @Test
    void shouldOverlapContained() {
        WorkSchedule s1 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .build();

        WorkSchedule s2 = WorkSchedule.builder()
                .dayOfWeek(1)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .build();

        assertTrue(s1.overlaps(s2));
        assertTrue(s2.overlaps(s1));
    }
}
