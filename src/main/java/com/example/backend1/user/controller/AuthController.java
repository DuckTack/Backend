package com.example.backend1.user.controller;

import com.example.backend1.user.dto.AuthDtos;
import com.example.backend1.user.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserService userService;

  public AuthController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping("/signup")
  public void signup(@RequestBody AuthDtos.SignupRequest req) {
    userService.signup(req);
  }

  @PostMapping("/login")
  public AuthDtos.TokenResponse login(@RequestBody AuthDtos.LoginRequest req) {
    return userService.login(req);
  }

  @GetMapping("/check-username")
  public boolean checkUsername(@RequestParam String username) {
    return userService.isUsernameAvailable(username);
  }

  @GetMapping("/check-email")
  public boolean checkEmail(@RequestParam String email) {
    return userService.isEmailAvailable(email);
  }

  @GetMapping("/check-phone")
  public boolean checkPhone(@RequestParam String phoneNumber) {
    return userService.isPhoneAvailable(phoneNumber);
  }

  @PostMapping("/send-email-code")
  public Map<String, String> sendEmailCode(@RequestBody Map<String, String> req) {
    return Map.of("devCode", "123456");
  }

  @PostMapping("/verify-email-code")
  public boolean verifyEmail(@RequestBody Map<String, String> req) {
    return "123456".equals(req.get("code"));
  }
}