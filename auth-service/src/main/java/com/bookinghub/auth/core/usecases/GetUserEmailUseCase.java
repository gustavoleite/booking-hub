package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.UserRepository;
import java.util.UUID;

public class GetUserEmailUseCase {

    private final UserRepository userRepository;

    public GetUserEmailUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String execute(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getEmail)
                .orElse(null);
    }
}
