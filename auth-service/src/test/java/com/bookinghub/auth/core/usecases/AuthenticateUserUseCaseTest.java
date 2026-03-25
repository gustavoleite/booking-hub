package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.exceptions.InactiveUserException;
import com.bookinghub.auth.core.exceptions.InvalidCredentialsException;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.bookinghub.auth.core.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticateUserUseCaseTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private TokenGenerator tokenGenerator;
    private AuthenticateUserUseCase authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        tokenGenerator = Mockito.mock(TokenGenerator.class);
        authenticateUserUseCase = new AuthenticateUserUseCase(userRepository, passwordEncoder, tokenGenerator);
    }

    @Test
    void shouldAuthenticateSuccessfully() {
        String email = "test@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        User user = new User(UUID.randomUUID(), email, hashedPassword, Set.of(Role.ROLE_CLIENT), true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);
        when(tokenGenerator.generateToken(user)).thenReturn("valid.jwt.token");

        String token = authenticateUserUseCase.execute(email, password);

        assertEquals("valid.jwt.token", token);
        verify(tokenGenerator).generateToken(user);
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenUserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> 
            authenticateUserUseCase.execute("wrong@example.com", "password")
        );
    }

    @Test
    void shouldThrowInvalidCredentialsExceptionWhenPasswordDoesNotMatch() {
        String email = "test@example.com";
        User user = new User(UUID.randomUUID(), email, "hashed", Set.of(Role.ROLE_CLIENT), true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> 
            authenticateUserUseCase.execute(email, "wrongpassword")
        );
    }

    @Test
    void shouldThrowInactiveUserExceptionWhenUserIsNotActive() {
        String email = "inactive@example.com";
        String password = "password123";
        String hashedPassword = "hashedPassword";
        User user = new User(UUID.randomUUID(), email, hashedPassword, Set.of(Role.ROLE_CLIENT), false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(password, hashedPassword)).thenReturn(true);

        assertThrows(InactiveUserException.class, () -> 
            authenticateUserUseCase.execute(email, password)
        );
    }
}
