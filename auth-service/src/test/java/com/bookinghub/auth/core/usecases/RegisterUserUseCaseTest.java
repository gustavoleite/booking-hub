package com.bookinghub.auth.core.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.exceptions.EmailAlreadyExistsException;
import com.bookinghub.auth.core.exceptions.InvalidRoleException;
import com.bookinghub.auth.core.exceptions.WeakPasswordException;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
    String password = "Password123";
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
  void shouldThrowEmailAlreadyExistsException_whenEmailIsTaken() {
    String email = "existing@example.com";
    when(userRepository.existsByEmail(email)).thenReturn(true);

    assertThrows(EmailAlreadyExistsException.class, () ->
        registerUserUseCase.execute(email, "Password123", Role.ROLE_CLIENT)
    );
  }

  @Test
  void shouldThrowWeakPasswordException_whenPasswordIsTooShort() {
    assertThrows(WeakPasswordException.class, () ->
        registerUserUseCase.execute("test@example.com", "Pass1", Role.ROLE_CLIENT)
    );
  }

  @Test
  void shouldThrowWeakPasswordException_whenPasswordHasNoUppercase() {
    assertThrows(WeakPasswordException.class, () ->
        registerUserUseCase.execute("test@example.com", "password123", Role.ROLE_CLIENT)
    );
  }

  @Test
  void shouldThrowWeakPasswordException_whenPasswordHasNoNumber() {
    assertThrows(WeakPasswordException.class, () ->
        registerUserUseCase.execute("test@example.com", "Password", Role.ROLE_CLIENT)
    );
  }

  @Test
  void shouldThrowInvalidRoleException_whenRoleIsNull() {
    assertThrows(InvalidRoleException.class, () ->
        registerUserUseCase.execute("test@example.com", "Password123", null)
    );
  }
}
