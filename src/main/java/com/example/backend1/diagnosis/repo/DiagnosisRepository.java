package com.example.backend1.diagnosis.repo;

import com.example.backend1.diagnosis.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
}
