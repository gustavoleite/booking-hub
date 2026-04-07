package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import lombok.Data;

@Data
public class AffiliationRequest {
  private boolean active = true;
  private List<WorkScheduleDto> workSchedules;
  private List<ServiceOfferingDto> serviceOfferings;

  @Data
  public static class WorkScheduleDto {
    private int dayOfWeek;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;
  }

  @Data
  public static class ServiceOfferingDto {
    private UUID providedServiceId;
    private BigDecimal price;
    private int durationMinutes;
  }
}
