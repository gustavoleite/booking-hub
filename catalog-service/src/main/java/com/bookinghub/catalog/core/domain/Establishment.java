package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Establishment {
    private final UUID id;
    private final String ownerId;
    private String name;
    private String cnpj;
    private String description;
    @Builder.Default
    private boolean active = true;
    private List<String> photos;
    private Address address;
    private List<BusinessHour> defaultBusinessHours;
    private List<ProvidedService> providedServices;

    public void updateDetails(String name, String description, List<String> photos) {
        this.name = name;
        this.description = description;
        this.photos = photos;
    }

    public void inactivate() {
        this.active = false;
    }
}
