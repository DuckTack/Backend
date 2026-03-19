package com.example.backend1.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthDtos {

  public record SignupRequest(
          @NotBlank @Size(min = 4, max = 50) String username,
          @NotBlank @Email @Size(max = 255) String email,
          @NotBlank @Size(min = 8, max = 72) String password,
          @NotBlank
          @Pattern(
                  regexp = "^[0-9\\-+() ]{8,20}$",
                  message = "전화번호 형식이 올바르지 않습니다."
          )
          String phoneNumber
  ) {}

  public record LoginRequest(
          @NotBlank String username,
          @NotBlank String password
  ) {}

  public record TokenResponse(
          String accessToken,
          String refreshToken,
          long refreshExpiresAtEpochSeconds
  ) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record LogoutRequest(@NotBlank String refreshToken) {}

  public record UsernameCheckResponse(boolean available) {}

  public record EmailCheckResponse(boolean available) {}

  public record PhoneCheckResponse(boolean available) {}

  public record SendEmailCodeRequest(@NotBlank @Email @Size(max = 255) String email) {}

  public record VerifyEmailCodeRequest(
          @NotBlank @Email @Size(max = 255) String email,
          @NotBlank @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.") String code
  ) {}

  public record VerifyEmailCodeResponse(boolean verified) {}
}
