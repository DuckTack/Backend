package com.example.backend1.user.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.user.dto.AuthDtos;
import com.example.backend1.user.service.UserService;
import jakarta.validation.Valid;
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

  @GetMapping("/check-username")
  public ApiResponse<AuthDtos.UsernameCheckResponse> checkUsername(@RequestParam String username) {
    return ApiResponse.ok(
            new AuthDtos.UsernameCheckResponse(
                    userService.isUsernameAvailable(username)
            )
    );
  }
}
