package com.bookinghub.auth.core.usecases;

import com.bookinghub.auth.core.domain.Role;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.exceptions.EmailAlreadyExistsException;
import com.bookinghub.auth.core.exceptions.InvalidRoleException;
import com.bookinghub.auth.core.exceptions.WeakPasswordException;
import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.UserRepository;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegisterUserUseCase {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  // A senha deve conter no mínimo 8 caracteres, uma letra maiúscula e um número.
  private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z]).{8,}$";
  private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

  public RegisterUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public User execute(String email, String password, Role role) {
    if (userRepository.existsByEmail(email)) {
      throw new EmailAlreadyExistsException("O e-mail informado já está em uso.");
    }

    if (password == null || !pattern.matcher(password).matches()) {
      throw new WeakPasswordException(
          "A senha deve conter no mínimo 8 caracteres, uma letra maiúscula e um número.");
    }

    if (role == null) {
      throw new InvalidRoleException(
          "Perfil de usuário inválido. Valores permitidos: "
              + "ROLE_CLIENT, ROLE_PROFESSIONAL, ROLE_OWNER.");
    }

    String passwordHash = passwordEncoder.encode(password);
    User user = new User(UUID.randomUUID(), email, passwordHash, Set.of(role), true);

    return userRepository.save(user);
  }
}
