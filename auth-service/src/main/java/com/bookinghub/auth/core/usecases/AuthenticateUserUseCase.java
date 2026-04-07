package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.exceptions.InactiveUserException;
import com.bookinghub.auth.core.exceptions.InvalidCredentialsException;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.bookinghub.auth.core.ports.UserRepository;

public class AuthenticateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder, TokenGenerator tokenGenerator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    public String execute(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("E-mail ou senha incorretos."));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("E-mail ou senha incorretos.");
        }

        if (!user.isActive()) {
            throw new InactiveUserException("O usuário está inativo.");
        }

        return tokenGenerator.generateToken(user);
    }
}
