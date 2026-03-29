package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class ProvidedService {
    private final UUID id;
    private final String title;
    private final String description;
    @Builder.Default
    private boolean active = true;

    public void inactivate() {
        this.active = false;
    }
}
