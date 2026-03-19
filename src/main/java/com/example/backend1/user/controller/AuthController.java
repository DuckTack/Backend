package com.example.backend1.user.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.user.dto.AuthDtos;
import com.example.backend1.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signup")
  public ApiResponse<Void> signup(@RequestBody @Valid AuthDtos.SignupRequest req) {
    userService.signup(req);
    return ApiResponse.ok("signed up", null);
  }

  @PostMapping("/login")
  public ApiResponse<AuthDtos.TokenResponse> login(@RequestBody @Valid AuthDtos.LoginRequest req) {
    return ApiResponse.ok(userService.login(req));
  }

  @PostMapping("/refresh")
  public ApiResponse<AuthDtos.TokenResponse> refresh(@RequestBody @Valid AuthDtos.RefreshRequest req) {
    return ApiResponse.ok(userService.refresh(req));
  }

  @PostMapping("/logout")
  public ApiResponse<Void> logout(
          Authentication authentication,
          @RequestBody @Valid AuthDtos.LogoutRequest req
  ) {
    userService.logout(authentication.getName(), req);
    return ApiResponse.ok("logged out", null);
  }

  @PostMapping("/email/send-code")
  public ApiResponse<Void> sendEmailCode(
          @RequestBody @Valid AuthDtos.SendEmailCodeRequest req
  ) {
    // TODO: 이메일 코드 발송 로직
    return ApiResponse.ok("email code sent", null);
  }

  @PostMapping("/email/verify-code")
  public ApiResponse<AuthDtos.VerifyEmailCodeResponse> verifyEmailCode(
          @RequestBody @Valid AuthDtos.VerifyEmailCodeRequest req
  ) {
    // TODO: 코드 검증 로직
    return ApiResponse.ok(
            new AuthDtos.VerifyEmailCodeResponse(true)
    );
  }

  @GetMapping("/check-username")
  public ApiResponse<AuthDtos.UsernameCheckResponse> checkUsername(@RequestParam String username) {
    return ApiResponse.ok(
            new AuthDtos.UsernameCheckResponse(
                    userService.isUsernameAvailable(username)
            )
    );
  }
}
