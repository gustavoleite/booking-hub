package com.bookinghub.catalog.infrastructure.adapters.in.rest.dto;

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
        private LocalTime openTime;
        private LocalTime closeTime;
    }

    @Data
    public static class ProvidedServiceDto {
        private String title;
        private String description;
    }
}
