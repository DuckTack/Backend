package com.example.backend1.history.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.history.dto.HistoryDtos;
import com.example.backend1.history.repo.HistoryRepository;
import com.example.backend1.user.repo.UserRepository;
import jakarta.persistence.criteria.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class HistoryService {

  private static final Logger log = LoggerFactory.getLogger(HistoryService.class);

  private final HistoryRepository historyRepository;
  private final UserRepository userRepository;

  public HistoryService(HistoryRepository historyRepository,
                        UserRepository userRepository) {
    this.historyRepository = historyRepository;
    this.userRepository = userRepository;
  }

  /**
   * 히스토리 목록 조회 (페이징 + 필터)
   */
  @Transactional(readOnly = true)
  public Page<HistoryDtos.HistoryItem> list(
          String username,
          OffsetDateTime from,
          OffsetDateTime to,
          List<AnalysisStatus> statuses,
          List<IssueType> issueTypes,
          Integer riskMin,
          Integer riskMax,
          Pageable pageable
  ) {
    var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    OffsetDateTime f = (from != null) ? from : OffsetDateTime.now().minusYears(10);
    OffsetDateTime t = (to != null) ? to : OffsetDateTime.now().plusDays(1);
    int rMin = (riskMin != null) ? riskMin : 0;
    int rMax = (riskMax != null) ? riskMax : 100;

    Specification<HistoryEntity> spec = (root, query, cb) -> {
      List<Predicate> p = new ArrayList<>();

      // 사용자 조건
      p.add(cb.equal(root.get("user").get("id"), user.getId()));

      // 날짜 조건
      p.add(cb.between(root.get("createdAt"), f, t));

      // riskScore null 방지 + 범위
      p.add(cb.isNotNull(root.get("riskScore")));
      p.add(cb.between(root.get("riskScore"), rMin, rMax));

      // 상태 필터
      if (statuses != null && !statuses.isEmpty()) {
        p.add(root.get("status").in(statuses));
      }

      // 문제 유형 필터
      if (issueTypes != null && !issueTypes.isEmpty()) {
        p.add(root.get("issueType").in(issueTypes));
      }

      return cb.and(p.toArray(new Predicate[0]));
    };

    Page<HistoryEntity> page = historyRepository.findAll(spec, pageable);

    return page.map(h -> {
      Long diagnosisId = h.resolveDiagnosisId();

      return new HistoryDtos.HistoryItem(
              h.getId(),
              diagnosisId,
              h.getStatus(),
              h.getRiskScore(),
              h.getIssueType(),
              h.getCreatedAt()
      );
    });
  }

  /**
   * 히스토리 상세 조회
   */
  @Transactional(readOnly = true)
  public HistoryDtos.HistoryDetail detail(String username, Long id) {
    var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    HistoryEntity h = historyRepository.findById(id)
            .filter(x -> x.getUser().getId().equals(user.getId()))
            .orElseThrow(() -> new ApiException(ErrorCode.HISTORY_NOT_FOUND));

    HistoryDtos.ReportMeta meta = null;
    if (h.getDiagnosis() != null && h.getDiagnosis().getReport() != null) {
        // 구 파이프라인: Diagnosis.report
        var report = h.getDiagnosis().getReport();
        meta = new HistoryDtos.ReportMeta(
                report.getStorageKey(),
                report.getContentType(),
                report.getSizeBytes()
        );
    } else if (h.getDiagnosisResult() != null && h.getDiagnosisResult().getPdfStorageKey() != null) {
        // 새 파이프라인: DiagnosisResult.pdfStorageKey
        meta = new HistoryDtos.ReportMeta(
                h.getDiagnosisResult().getPdfStorageKey(),
                "application/pdf",
                0L
        );
    }

    Long diagnosisId = h.resolveDiagnosisId();

    // 진단 이미지 URL: 새 파이프라인(DiagnosisResult) 우선, 없으면 null
    String imageUrl = null;
    if (h.getDiagnosisResult() != null) {
        imageUrl = h.getDiagnosisResult().getImageUrl();
    }

    return new HistoryDtos.HistoryDetail(
            h.getId(),
            diagnosisId,
            h.getStatus(),
            h.getRiskScore(),
            h.getIssueType(),
            h.getCreatedAt(),
            meta,
            imageUrl
    );
  }

  /**
   * 히스토리 여러 개 삭제
   */
  @Transactional
  public void bulkDelete(String username, List<Long> ids) {
    var user = userRepository.findByUsername(username)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    historyRepository.deleteByIdInAndUserId(ids, user.getId());

    log.info("History deleted: user={}, count={}", username, ids.size());
  }
}