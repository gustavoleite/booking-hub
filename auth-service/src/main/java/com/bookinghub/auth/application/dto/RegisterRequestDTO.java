package com.bookinghub.auth.application.dto;

import com.bookinghub.auth.core.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
    @Schema(description = "User email address", example = "cliente@teste.com")
    @NotBlank @Email String email,

    @Schema(description = "User password", example = "SenhaForte123!", minLength = 8)
    @NotBlank @Size(min = 8) String password,

    @Schema(description = "User role", example = "ROLE_CLIENT")
    @NotNull Role role
) {}
