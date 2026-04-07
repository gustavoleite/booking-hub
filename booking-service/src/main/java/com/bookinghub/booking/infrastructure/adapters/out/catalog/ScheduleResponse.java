package com.bookinghub.booking.infrastructure.adapters.out.catalog;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

public record ScheduleResponse(
        @JsonProperty("active") boolean active,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("durationMinutes") int durationMinutes,
        @JsonProperty("fixedSchedule") List<DayScheduleResponse> fixedSchedule
) {
  public record DayScheduleResponse(
      @JsonProperty("dayOfWeek") int dayOfWeek,
      @JsonProperty("startTime") String startTime,
      @JsonProperty("endTime") String endTime
  ) {
  }
}
