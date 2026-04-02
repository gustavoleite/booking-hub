package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalTime;
import java.util.List;

@Data
public class EstablishmentRequest {
    private String name;
    private String cnpj;
    private String description;
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
