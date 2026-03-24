package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterUserUseCaseTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        userRepository = Mockito.mock(UserRepository.class);
        passwordEncoder = Mockito.mock(PasswordEncoder.class);
        registerUserUseCase = new RegisterUserUseCase(userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        String email = "test@example.com";
        String password = "password123";
        Role role = Role.ROLE_CLIENT;

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registeredUser = registerUserUseCase.execute(email, password, role);

        assertNotNull(registeredUser);
        assertEquals(email, registeredUser.getEmail());
        assertEquals("hashedPassword", registeredUser.getPasswordHash());
        assertTrue(registeredUser.getRoles().contains(role));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        String email = "existing@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        assertThrows(RuntimeException.class, () -> 
            registerUserUseCase.execute(email, "password", Role.ROLE_CLIENT)
        );
    }
}
