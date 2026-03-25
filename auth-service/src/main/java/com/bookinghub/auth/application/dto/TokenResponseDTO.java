package com.bookinghub.auth.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenResponseDTO(
    @Schema(description = "JWT Access Token")
    String accessToken,

    @Schema(description = "Token expiration time in seconds", example = "3600")
    int expiresIn,

    @Schema(description = "Type of the token", example = "Bearer")
    String tokenType
) {}
