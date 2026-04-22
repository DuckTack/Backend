package com.example.backend1.user.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.user.dto.UserDtos;
import com.example.backend1.user.service.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public ApiResponse<UserDtos.MeResponse> me(Authentication authentication) {
    return ApiResponse.ok(userService.me(authentication.getName()));
  }

  @PutMapping("/me")
  public ApiResponse<UserDtos.MeResponse> updateMe(
          Authentication authentication,
          @RequestBody UserDtos.UpdateProfileRequest req
  ) {
    return ApiResponse.ok(userService.updateProfile(authentication.getName(), req));
  }
}