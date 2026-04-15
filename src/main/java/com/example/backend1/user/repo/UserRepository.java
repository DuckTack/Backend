package com.example.backend1.user.repo;

import com.example.backend1.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 존재 여부 확인
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

    // 조회
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    // 🔥 비밀번호 재설정용 (핵심)
    Optional<User> findByUsernameAndEmail(String username, String email);
}