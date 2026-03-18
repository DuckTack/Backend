package com.example.backend1.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthDtos {

    public record SignupRequest(
            @NotBlank @Size(min = 4, max = 50) String username,
            @NotBlank @Size(min = 8, max = 72) String password,
            @NotBlank String email,
            String phoneNumber,
            @NotBlank String residenceType,
            Boolean isRenter,
            Boolean emailVerified
    ) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record TokenResponse(
            String accessToken
    ) {}
}

