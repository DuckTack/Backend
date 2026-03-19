package com.example.backend1.user.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class EmailVerificationService {

    private final Map<String, String> codeStorage = new HashMap<>();

    // 인증코드 발송
    public void sendCode(String email) {
        String code = generateCode();
        codeStorage.put(email, code);

        // TODO: 실제 메일 전송 로직 연결
        System.out.println("EMAIL CODE [" + email + "] : " + code);
    }

    // 코드 검증
    public boolean verifyCode(String email, String code) {
        return code.equals(codeStorage.get(email));
    }

    // 인증 여부 확인
    public boolean isVerified(String email) {
        return codeStorage.containsKey(email);
    }

    private String generateCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }
}