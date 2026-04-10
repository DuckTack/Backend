package com.example.backend1.user.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.user.domain.EmailVerification;
import com.example.backend1.user.repo.EmailVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
public class EmailVerificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailVerificationService.class);
    private static final SecureRandom random = new SecureRandom();

    private final EmailVerificationRepository emailVerificationRepository;
    private final JavaMailSender mailSender;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Value("${app.email.from:no-reply@example.com}")
    private String fromEmail;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${app.email.verification.ttl-minutes:10}")
    private long ttlMinutes;

    public EmailVerificationService(
            EmailVerificationRepository emailVerificationRepository,
            JavaMailSender mailSender,
            org.springframework.security.crypto.password.PasswordEncoder passwordEncoder
    ) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void sendCode(String email) {
        String code = generate6DigitCode();
        String hash = passwordEncoder.encode(code);
        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(ttlMinutes);

        emailVerificationRepository.save(new EmailVerification(email, hash, expiresAt));

        if (mailHost == null || mailHost.isBlank()) {
            log.warn("MAIL_HOST not set. Email verification code for {} is {}", email, code);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(email);
            msg.setSubject("[DuckTack] 이메일 인증 코드");
            msg.setText("인증 코드: " + code + "\n\n" +
                    "유효시간: " + ttlMinutes + "분");
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}", email, e);
            throw new ApiException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }

    @Transactional
    public boolean verifyCode(String email, String code) {
        EmailVerification latest = emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .orElseThrow(() -> new ApiException(ErrorCode.EMAIL_VERIFICATION_INVALID));

        OffsetDateTime now = OffsetDateTime.now();
        if (latest.isVerified()) {
            return true;
        }
        if (latest.isExpired(now)) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }
        if (!passwordEncoder.matches(code, latest.getCodeHash())) {
            throw new ApiException(ErrorCode.EMAIL_VERIFICATION_INVALID);
        }

        latest.markVerified(now);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isVerified(String email) {
        return emailVerificationRepository.findTopByEmailOrderByCreatedAtDesc(email)
                .filter(EmailVerification::isVerified)
                .filter(v -> !v.isExpired(OffsetDateTime.now()))
                .isPresent();
    }

    private static String generate6DigitCode() {
        int n = random.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}