package com.bookinghub.auth.core.ports;

import com.bookinghub.auth.core.domain.User;

public interface TokenGenerator {
    String generateToken(User user);
}
