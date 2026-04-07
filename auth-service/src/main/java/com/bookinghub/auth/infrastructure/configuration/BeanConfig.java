package com.bookinghub.auth.infrastructure.configuration;

import com.bookinghub.auth.core.ports.PasswordEncoder;
import com.bookinghub.auth.core.ports.TokenGenerator;
import com.bookinghub.auth.core.ports.UserRepository;
import com.bookinghub.auth.core.usecases.AuthenticateUserUseCase;
import com.bookinghub.auth.core.usecases.RegisterUserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

  @Bean
  public RegisterUserUseCase registerUserUseCase(
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    return new RegisterUserUseCase(userRepository, passwordEncoder);
  }

  @Bean
  public AuthenticateUserUseCase authenticateUserUseCase(UserRepository userRepository,
      PasswordEncoder passwordEncoder, TokenGenerator tokenGenerator) {
    return new AuthenticateUserUseCase(userRepository, passwordEncoder, tokenGenerator);
  }
}
