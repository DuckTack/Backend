package com.example.backend1.diagnosis.repo;

import com.example.backend1.diagnosis.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
    Optional<Diagnosis> findByIdAndUserUsername(Long id, String username);
}
