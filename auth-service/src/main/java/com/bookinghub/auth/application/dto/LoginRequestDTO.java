package com.bookinghub.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @Schema(description = "User email address", example = "cliente@teste.com")
    @NotBlank @Email String email,

    @Schema(description = "User password", example = "SenhaForte123!")
    @NotBlank String password
) {}
