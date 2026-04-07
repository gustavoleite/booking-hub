package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import lombok.Data;

@Data
public class EstablishmentRequest {
  private String name;
  private String cnpj;
  private String description;
  @Valid
  private AddressDto address;
  private List<BusinessHourDto> businessHours;
  private List<ProvidedServiceDto> services;

  @Data
  public static class AddressDto {
    private String street;
    private String number;
    private String city;
    private String state;
    private String zipCode;
    @NotNull
    private BigDecimal latitude;
    @NotNull
    private BigDecimal longitude;
  }

  @Data
  public static class BusinessHourDto {
    private int dayOfWeek;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime closeTime;
  }

  @Data
  public static class ProvidedServiceDto {
    private String title;
    private String description;
  }
}
