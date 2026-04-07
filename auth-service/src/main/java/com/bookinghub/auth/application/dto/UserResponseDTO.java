package com.bookinghub.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record UserResponseDTO(
    @Schema(description = "User unique identifier")
    UUID id,

    @Schema(description = "User email address", example = "cliente@teste.com")
    String email
) {
}
