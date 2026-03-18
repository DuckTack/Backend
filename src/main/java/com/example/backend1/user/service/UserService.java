package com.example.backend1.user.service;

import com.example.backend1.security.JwtTokenProvider;
import com.example.backend1.user.domain.User;
import com.example.backend1.user.dto.AuthDtos;
import com.example.backend1.user.dto.UserDtos;
import com.example.backend1.user.repo.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;

  public UserService(
          UserRepository userRepository,
          PasswordEncoder passwordEncoder,
          AuthenticationManager authenticationManager,
          JwtTokenProvider jwtTokenProvider
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Transactional
  public void signup(AuthDtos.SignupRequest req) {

    if (userRepository.existsByUsername(req.username()))
      throw new RuntimeException("USERNAME_TAKEN");

    if (userRepository.existsByEmail(req.email()))
      throw new RuntimeException("EMAIL_TAKEN");

    if (req.phoneNumber() != null &&
            userRepository.existsByPhoneNumber(req.phoneNumber()))
      throw new RuntimeException("PHONE_TAKEN");

    if (!Boolean.TRUE.equals(req.emailVerified()))
      throw new RuntimeException("EMAIL_NOT_VERIFIED");

    String hash = passwordEncoder.encode(req.password());

    userRepository.save(
            new User(
                    req.username(),
                    hash,
                    req.email(),
                    req.phoneNumber(),
                    req.residenceType(),
                    req.isRenter()
            )
    );
  }

  @Transactional(readOnly = true)
  public boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
  }

  @Transactional(readOnly = true)
  public boolean isEmailAvailable(String email) {
    return !userRepository.existsByEmail(email);
  }

  @Transactional(readOnly = true)
  public boolean isPhoneAvailable(String phone) {
    return !userRepository.existsByPhoneNumber(phone);
  }

  @Transactional(readOnly = true)
  public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {

    Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );

    User user = userRepository.findByUsername(req.username())
            .orElseThrow();

    String token = jwtTokenProvider.createAccessToken(
            auth,
            user.getId(),
            user.getUsername()
    );

    return new AuthDtos.TokenResponse(token);
  }

  // 🔥 추가된 부분

  @Transactional(readOnly = true)
  public UserDtos.MeResponse me(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow();

    return new UserDtos.MeResponse(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            null,
            null,
            null
    );
  }

  @Transactional
  public UserDtos.MeResponse updateProfile(String username, UserDtos.UpdateProfileRequest req) {
    User user = userRepository.findByUsername(username)
            .orElseThrow();

    user.setPhoneNumber(req.phoneNumber());
    user.setResidenceType(req.residenceType().name());

    return new UserDtos.MeResponse(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            req.address(),
            req.residenceType(),
            req.rentType()
    );
  }
}