package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.bookinghub.auth.core.ports.UserRepository;

public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public AuthenticateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public String execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        if (!user.isActive()) {
            throw new RuntimeException("User is not active");
        }

        return tokenGenerator.generateToken(user);
    }
}
