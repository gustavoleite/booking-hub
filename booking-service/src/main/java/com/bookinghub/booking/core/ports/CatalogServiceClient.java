package com.bookinghub.booking.core.ports;

import com.bookinghub.booking.core.domain.ScheduleInfo;
import java.util.UUID;

public interface CatalogServiceClient {
    ScheduleInfo getSchedule(UUID establishmentId, UUID professionalId, UUID serviceId);
}
