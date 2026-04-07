package com.bookinghub.catalog.core.domain;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
    @Builder.Default
    private List<ProvidedService> providedServices = new java.util.ArrayList<>();

    public void updateDetails(
            String name, String description, List<String> photos, Address address) {
        this.name = name;
        this.description = description;
        this.photos = photos;
        this.address = address;
    }

    public void inactivate() {
        this.active = false;
    }

    public void addProvidedService(ProvidedService service) {
        if (this.providedServices == null) {
            this.providedServices = new java.util.ArrayList<>();
        }
        this.providedServices.add(service);
    }
}
