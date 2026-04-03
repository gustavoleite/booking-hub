package com.bookinghub.booking.core.domain;

import java.math.BigDecimal;
import java.util.List;

public record ScheduleInfo(
        boolean active,
        BigDecimal price,
        int durationMinutes,
        List<DaySchedule> workSchedule
) {}
