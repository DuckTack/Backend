package com.example.backend1.user.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.security.JwtTokenProvider;
import com.example.backend1.user.domain.User;
import com.example.backend1.user.dto.AuthDtos;
import com.example.backend1.user.dto.UserDtos;
import com.example.backend1.user.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

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

  /* =========================
     회원가입
     ========================= */
  @Transactional
  public void signup(AuthDtos.SignupRequest req) {
    if (userRepository.existsByUsername(req.username())) {
      throw new ApiException(ErrorCode.USERNAME_DUPLICATE);
    }

    String hash = passwordEncoder.encode(req.password());

    userRepository.save(
            new User(
                    req.username(),
                    hash,
                    req.phoneNumber()
            )
    );

    log.info("User signed up: {}", req.username());
  }

  /* =========================
     아이디 중복 체크
     ========================= */
  @Transactional(readOnly = true)
  public boolean isUsernameAvailable(String username) {
    return !userRepository.existsByUsername(username);
  }

  /* =========================
     로그인
     ========================= */
  @Transactional(readOnly = true)
  public AuthDtos.TokenResponse login(AuthDtos.LoginRequest req) {
    Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
    );

    User user = userRepository.findByUsername(req.username())
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    String token = jwtTokenProvider.createAccessToken(
            auth,
            user.getId(),
            user.getUsername()
    );

    log.info("User logged in: {}", req.username());
    return new AuthDtos.TokenResponse(token);
  }

  /* =========================
     내 정보 조회
     ========================= */
  @Transactional(readOnly = true)
  public UserDtos.MeResponse me(String username) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    return new UserDtos.MeResponse(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getResidenceType(),
            user.getRentType()
    );
  }

  /* =========================
     프로필 수정
     ========================= */
  @Transactional
  public UserDtos.MeResponse updateProfile(String username, UserDtos.UpdateProfileRequest req) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    user.updateProfile(
            req.residenceType(),
            req.rentType(),
            req.phoneNumber(),
            req.address()
    );

    log.info("Profile updated: {}", username);

    return new UserDtos.MeResponse(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            user.getAddress(),
            user.getResidenceType(),
            user.getRentType()
    );
  }
}
