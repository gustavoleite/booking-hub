package com.bookinghub.auth.infrastructure.adapters.in.rest;

import com.bookinghub.auth.application.dto.*;
import com.bookinghub.auth.core.domain.User;
import com.bookinghub.auth.core.usecases.AuthenticateUserUseCase;
import com.bookinghub.auth.core.usecases.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;

    public AuthController(RegisterUserUseCase registerUserUseCase, AuthenticateUserUseCase authenticateUserUseCase) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
    }

    @PostMapping("register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "210", description = "User successfully registered"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public ResponseEntity<UserResponseDTO> register(@RequestBody @Valid RegisterRequestDTO request) {
        User user = registerUserUseCase.execute(request.email(), request.password(), request.role());
        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponseDTO(user.getId(), user.getEmail()));
    }

    @PostMapping("login")
    @Operation(summary = "Authenticate user", description = "Authenticates user and returns a JWT access token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<TokenResponseDTO> login(@RequestBody @Valid LoginRequestDTO request) {
        String token = authenticateUserUseCase.execute(request.email(), request.password());
        return ResponseEntity.ok(new TokenResponseDTO(token, 3600, "Bearer"));
    }
}
