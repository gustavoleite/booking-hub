package com.bookinghub.catalog.core.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Affiliation {
    private final UUID id;
    private final UUID establishmentId;
    private final UUID professionalId;
    private boolean active;
    private List<WorkSchedule> workSchedules;
    private List<ServiceOffering> serviceOfferings;

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public void updateSchedules(List<WorkSchedule> newSchedules) {
        // Business Rule 2: No overlapping schedules for the same day
        for (int i = 0; i < newSchedules.size(); i++) {
            for (int j = i + 1; j < newSchedules.size(); j++) {
                if (newSchedules.get(i).overlaps(newSchedules.get(j))) {
                    throw new RuntimeException("Overlapping schedules for the same professional");
                }
            }
        }
        this.workSchedules = newSchedules;
    }
}
