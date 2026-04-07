package com.bookinghub.catalog.core.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BusinessHour {
  private final int dayOfWeek; // 1 (Monday) to 7 (Sunday)
  @JsonFormat(pattern = "HH:mm:ss")
  private final LocalTime openTime;
  @JsonFormat(pattern = "HH:mm:ss")
  private final LocalTime closeTime;

  public boolean isWithin(LocalTime start, LocalTime end) {
    return !start.isBefore(openTime) && !end.isAfter(closeTime);
  }
}
