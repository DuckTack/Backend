package com.example.backend1.report.repo;

import com.example.backend1.report.entity.ReportDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportDraftRepository extends JpaRepository<ReportDraft, Long> {

    /** diagnosisId 로 드래프트 조회 (진단 1개당 드래프트 최대 1개) */



    Optional<ReportDraft> findByDiagnosisId(Long diagnosisId);
}
