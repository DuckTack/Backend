package com.example.backend1.report.service;

import com.example.backend1.ai.DecisionService;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.diagnosis.domain.Diagnosis;
import com.example.backend1.diagnosis.domain.ReportMetadata;
import com.example.backend1.diagnosis.repo.DiagnosisRepository;
import com.example.backend1.report.dto.ReportDraftDto;
import com.example.backend1.report.entity.ReportDraft;
import com.example.backend1.report.repo.ReportDraftRepository;
import com.example.backend1.storage.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class ReportService {

    private final DiagnosisRepository diagnosisRepository;
    private final ReportDraftRepository reportDraftRepository;
    private final ReportRenderer reportRenderer;
    private final PdfService pdfService;
    private final FileService fileService;
    private final FileStorage fileStorage;
    private final FileRecordRepository fileRecordRepository;

    public ReportService(
            DiagnosisRepository diagnosisRepository,
            ReportDraftRepository reportDraftRepository,
            ReportRenderer reportRenderer,
            PdfService pdfService,
            FileService fileService,
            FileStorage fileStorage,                     // ⭐ 추가
            FileRecordRepository fileRecordRepository    // ⭐ 추가
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.reportDraftRepository = reportDraftRepository;
        this.reportRenderer = reportRenderer;
        this.pdfService = pdfService;
        this.fileService = fileService;
        this.fileStorage = fileStorage;                 // ⭐ 추가
        this.fileRecordRepository = fileRecordRepository; // ⭐ 추가
    }

    // =========================
    // PDF 생성
    // =========================
    @Transactional
    public ReportMetadata generateAndAttach(
            Long diagnosisId,
            String summary,
            double confidence,
            String decision,
            List<String> reasons,
            List<String> cautionNotes,
            DecisionService.Estimate estimate
    ) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));


        // ⭐ 기존 코드 유지 + 아래만 추가
        ReportDraft draft = reportDraftRepository.findByDiagnosisId(diagnosisId).orElse(null);
        System.out.println("PDF용 draft: " + draft);
// ⭐ BEFORE 이미지
        List<String> beforeImages = fileRecordRepository
                .findByDiagnosisIdAndCategory(diagnosisId, FileCategory.BEFORE_IMAGE)
                .stream()
                .map(f -> fileStorage.getPublicUrl(f.getStorageKey()))
                .toList();

// ⭐ AFTER 이미지
        List<String> afterImages = fileRecordRepository
                .findByDiagnosisIdAndCategory(diagnosisId, FileCategory.AFTER_IMAGE)
                .stream()
                .map(f -> fileStorage.getPublicUrl(f.getStorageKey()))
                .toList();

// ⭐ 기존 toHtml 호출만 교체
        String html = reportRenderer.toHtml(
                diagnosis,
                summary,
                confidence,
                decision,
                reasons,
                cautionNotes,
                estimate,
                draft,
                beforeImages,
                afterImages
        );

        byte[] pdfBytes = pdfService.htmlToPdf(html);
        var saved = fileService.saveReportPdf(diagnosis.getUser().getUsername(), pdfBytes);

        ReportMetadata metadata;

        if (diagnosis.getReport() != null) {
            metadata = diagnosis.getReport();
            metadata.update(saved.key(), saved.contentType(), saved.sizeBytes());
        } else {
            metadata = new ReportMetadata(saved.key(), saved.contentType(), saved.sizeBytes());
            diagnosis.attachReport(metadata);
        }

        return metadata;
    }

    // =========================
    // 드래프트 저장
    // =========================

    // =========================
// 드래프트 저장 (수정 완료)
// =========================

    @Transactional
    public ReportDraftDto.DraftResponse saveDraft(String username, Long diagnosisId, ReportDraftDto.DraftRequest req) {

        System.out.println("🔥 SAVE API 들어옴");
        System.out.println("🔥 username = " + username);
        System.out.println("🔥 diagnosisId = " + diagnosisId);
        System.out.println("🔥 req = " + req);
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        ReportDraft draft = reportDraftRepository.findByDiagnosisId(diagnosisId)
                .orElseGet(() -> reportDraftRepository.save(new ReportDraft(diagnosis)));
        System.out.println("🔥 draft 있음? " + draft);

        // ⭐ repairDate → LocalDate 변환
        LocalDate completionDate = null;
        if (req.repairDate() != null && !req.repairDate().isBlank()) {
            try {
                completionDate = LocalDate.parse(req.repairDate());
            } catch (Exception ignored) {}
        }

        // ⭐ 핵심: 프론트 기준으로 매핑
        draft.update(
                req.repairMethod(),
                completionDate,
                req.contractorName(),
                req.contractorContact(),
                req.repairSummary(),
                req.actualCostKrw(),
                req.notes(),
                req.materialCost(),
                req.laborCost(),
                req.diyMaterialsUsed(),
                req.diyMaterialCost(),
                req.diyWorkMemo()
        );


        // ⭐ totalCost 따로
        draft.setTotalCost(req.totalCost());

        // ⭐ 저장 + flush (DB 반영 강제)
        reportDraftRepository.save(draft);
        reportDraftRepository.flush();

        return toDraftResponse(diagnosisId, draft);
    }
    // =========================
    // PDF URL
    // =========================
    @Transactional(readOnly = true)
    public String getPdfPublicUrl(String username, Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (diagnosis.getReport() == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "PDF가 아직 생성되지 않았습니다.");
        }

        return fileService.getOwnedPublicUrl(username, diagnosis.getReport().getStorageKey());
    }

    // =========================
    // 드래프트 조회
    // =========================
    @Transactional(readOnly = true)
    public ReportDraftDto.DraftResponse getDraft(String username, Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        ReportDraft draft = reportDraftRepository.findByDiagnosisId(diagnosisId).orElse(null);

        return toDraftResponse(diagnosisId, draft);
    }

    // =========================
    // PDF 다운로드
    // =========================
    @Transactional(readOnly = true)
    public byte[] downloadByDiagnosisId(String username, Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (diagnosis.getReport() == null) {
            throw new ApiException(ErrorCode.INVALID_INPUT, "PDF 없음");
        }

        return fileService.load(diagnosis.getReport().getStorageKey());
    }

    // =========================
    // 리포트 목록
    // =========================
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getMyReports(String username) {

        List<Diagnosis> diagnoses = diagnosisRepository.findByUserUsername(username);

        return diagnoses.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(d -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("diagnosisId", d.getId());
                    map.put("issueType", d.getIssueType());
                    map.put("riskScore", d.getRiskScore());
                    map.put("createdAt", d.getCreatedAt());

                    String status;
                    if (d.getReport() != null) status = "READY";
                    else if ("FAILED".equals(d.getStatus().name())) status = "FAILED";
                    else status = "GENERATING";

                    map.put("status", status);
                    return map;
                })
                .toList();
    }
    public List<String> uploadImages(String username, Long diagnosisId, List<MultipartFile> files, String type) {

        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        FileCategory category =
                type.equals("AFTER") ? FileCategory.AFTER_IMAGE : FileCategory.BEFORE_IMAGE;

        List<String> keys = new ArrayList<>();

        for (MultipartFile file : files) {

            StoredFile saved = fileStorage.save(file);

            FileRecord record = new FileRecord(diagnosis.getUser(), category, saved, diagnosisId);

            fileRecordRepository.save(record);

            keys.add(saved.key());
        }

        return keys;
    }
    // =========================
    // 상태 맵
    // =========================
    @Transactional(readOnly = true)
    public Map<Long, String> getStatusMap(String username) {

        List<Diagnosis> diagnoses = diagnosisRepository.findByUserUsername(username);

        Map<Long, String> result = new HashMap<>();

        for (Diagnosis d : diagnoses) {
            String status;
            if (d.getReport() != null) status = "READY";
            else if ("FAILED".equals(d.getStatus().name())) status = "FAILED";
            else status = "GENERATING";

            result.put(d.getId(), status);
        }

        return result;
    }

    // =========================
    // 자동 생성
    // =========================
    @Transactional
    public void generateForUser(String username, Long diagnosisId) {
        System.out.println("🔥 GENERATE API 들어옴");
        System.out.println("🔥 generate username = " + username);
        System.out.println("🔥 generate diagnosisId = " + diagnosisId);
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        DecisionService.Estimate estimate = buildEstimateFromRisk(diagnosis.getRiskScore());
        String decision = diagnosis.getRiskScore() >= 70 ? "PRO" : "DIY";

        generateAndAttach(
                diagnosisId,
                "AI 분석 결과 기반 리포트",
                0.0,
                decision,
                List.of("문제 감지됨"),
                List.of("추가 점검 필요"),
                estimate
        );
    }

    private ReportDraftDto.DraftResponse toDraftResponse(Long diagnosisId, ReportDraft draft) {

        if (draft == null) {
            return new ReportDraftDto.DraftResponse(
                    diagnosisId,
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null,
                    null, null,   // ⭐ 이것도 맞춰야 함
                    null
            );
        }

        return new ReportDraftDto.DraftResponse(
                diagnosisId,
                draft.getRepairMethod(),
                draft.getCompletionDate() != null ? draft.getCompletionDate().toString() : null,
                draft.getCompanyOrPersonName(),
                draft.getContactInfo(),
                draft.getWorkSummary(),
                draft.getActualCostKrw(),
                draft.getMemo(),
                draft.getMaterialCost(),
                draft.getLaborCost(),
                draft.getTotalCost(),
                draft.getDiyMaterialsUsed(),
                draft.getDiyMaterialCost(),
                draft.getDiyWorkMemo(),

                null,   // ⭐ beforeImageUris
                null,   // ⭐ afterImageUris

                draft.getUpdatedAt() != null ? draft.getUpdatedAt().toLocalDateTime() : null
        );
    }

    private DecisionService.Estimate buildEstimateFromRisk(int riskScore) {
        if (riskScore >= 70) return new DecisionService.Estimate("HIGH", 70000, 150000);
        if (riskScore >= 40) return new DecisionService.Estimate("MEDIUM", 30000, 70000);
        return new DecisionService.Estimate("LOW", 10000, 30000);
    }
}