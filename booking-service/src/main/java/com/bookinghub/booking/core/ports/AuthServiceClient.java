package com.bookinghub.booking.core.ports;

import java.util.UUID;

public interface AuthServiceClient {
    String getUserEmail(UUID userId);
}
