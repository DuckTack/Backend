package com.example.backend1.user.repo;

import com.example.backend1.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);
  boolean existsByEmail(String email);          // 필수
  boolean existsByPhoneNumber(String phoneNumber); // 필수

  Optional<User> findByUsername(String username);
}