package com.bookinghub.auth.application.dto;

public record TokenResponseDTO(String accessToken, int expiresIn, String tokenType) {}
