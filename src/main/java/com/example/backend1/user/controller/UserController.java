package com.example.backend1.user.controller;

import com.example.backend1.user.dto.UserDtos;
import com.example.backend1.user.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping("/me")
  public UserDtos.MeResponse me(Authentication authentication) {
    return userService.me(authentication.getName());
  }

  @PutMapping("/me")
  public UserDtos.MeResponse updateMe(
          Authentication authentication,
          @RequestBody UserDtos.UpdateProfileRequest req
  ) {
    return userService.updateProfile(authentication.getName(), req);
  }
}