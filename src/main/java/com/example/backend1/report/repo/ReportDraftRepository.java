package com.example.backend1.report.repo;

import com.example.backend1.report.entity.ReportDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReportDraftRepository extends JpaRepository<ReportDraft, Long> {

    /** 구 파이프라인: diagnosisId 로 드래프트 조회 */
    Optional<ReportDraft> findByDiagnosis_Id(Long diagnosisId);

    /** 새 파이프라인: diagnosisResultId 로 드래프트 조회 */
    Optional<ReportDraft> findByDiagnosisResult_Id(Long diagnosisResultId);
}
