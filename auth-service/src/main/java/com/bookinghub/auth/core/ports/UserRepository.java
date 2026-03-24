package com.bookinghub.auth.core.ports;

import com.bookinghub.auth.core.domain.User;
import java.util.Optional;

public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
