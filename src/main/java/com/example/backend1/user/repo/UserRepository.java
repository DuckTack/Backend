package com.example.backend1.user.repo;

import com.example.backend1.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

  boolean existsByUsername(String username);
  boolean existsByEmail(String email);
  boolean existsByPhoneNumber(String phoneNumber);
<<<<<<< HEAD

  Optional<User> findByUsername(String username);
}
=======
}
>>>>>>> 54853b61a9ad007f59f1bc6b2ecbd171b008dcd6
