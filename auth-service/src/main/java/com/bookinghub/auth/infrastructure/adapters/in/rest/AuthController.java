package com.bookinghub.auth.infrastructure.adapters.in.rest;

import com.bookinghub.auth.application.dto.*;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.usecases.AuthenticateUserUseCase;
import com.bookinghub.auth.core.usecases.RegisterUserUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request) {
        User user = registerUserUseCase.execute(request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(user.getId(), user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        String token = authenticateUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponseDTO(token, 3600, "Bearer"));
    }
}
