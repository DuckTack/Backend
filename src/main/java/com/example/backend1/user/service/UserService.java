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
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            RefreshTokenService refreshTokenService,
            EmailVerificationService emailVerificationService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenService = refreshTokenService;
        this.emailVerificationService = emailVerificationService;
    }

    @Transactional
    public void signup(AuthDtos.SignupRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ApiException(ErrorCode.USERNAME_DUPLICATE);
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ApiException(ErrorCode.EMAIL_DUPLICATE);
        }
        if (userRepository.existsByPhoneNumber(req.phoneNumber())) {
            throw new ApiException(ErrorCode.PHONE_DUPLICATE);
        }

        if (!emailVerificationService.isVerified(req.email())) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_REQUIRED);
        }

        String hash = passwordEncoder.encode(req.password());

        userRepository.save(
                new User(
                        req.username(),
                        req.email(),
                        hash,
                        req.phoneNumber(),
                        req.residenceType(),
                        req.rentType(),
                        req.address()
                )
        );

        log.info("User signed up: {}", req.username());
    }

    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        boolean exists = userRepository.existsByUsername(username);
        log.debug("isUsernameAvailable: username={} exists={}", username, exists);
        return !exists;
    }

    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public boolean isPhoneAvailable(String phoneNumber) {
        return !userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional
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

        var refresh = refreshTokenService.issue(user);

        log.info("User logged in: {}", req.username());
        return new AuthDtos.TokenResponse(
                token,
                refresh.refreshToken(),
                refresh.expiresAt().toEpochSecond(),
                user.getRole().name()
        );
    }

    @Transactional
    public AuthDtos.TokenResponse refresh(AuthDtos.RefreshRequest req) {
        var current = refreshTokenService.validateActive(req.refreshToken());
        var rotated = refreshTokenService.rotate(current);

        User user = current.getUser();
        Authentication auth = new UsernamePasswordAuthenticationToken(user.getUsername(), null, java.util.List.of());
        String access = jwtTokenProvider.createAccessToken(auth, user.getId(), user.getUsername());

        return new AuthDtos.TokenResponse(
                access,
                rotated.refreshToken(),
                rotated.expiresAt().toEpochSecond(),
                user.getRole().name()
        );
    }

    @Transactional
    public void logout(String username, AuthDtos.LogoutRequest req) {
        var current = refreshTokenService.validateActive(req.refreshToken());
        if (!current.getUser().getUsername().equals(username)) {
            throw new ApiException(ErrorCode.ACCESS_DENIED);
        }
        current.revoke(java.time.OffsetDateTime.now(), null);
    }

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