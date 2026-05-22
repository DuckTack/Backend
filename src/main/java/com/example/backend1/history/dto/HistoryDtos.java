package com.example.backend1.history.dto;

import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.IssueType;
import jakarta.validation.constraints.NotEmpty;

import java.time.OffsetDateTime;
import java.util.List;

public class HistoryDtos {

  public record HistoryItem(
      Long id,
      Long diagnosisId,
      AnalysisStatus status,
      Integer riskScore,
      IssueType issueType,
      OffsetDateTime createdAt
  ) {}

  public record ReportMeta(String storageKey, String contentType, long sizeBytes) {}

  public record HistoryDetail(
      Long id,
      Long diagnosisId,
      AnalysisStatus status,
      Integer riskScore,
      IssueType issueType,
      OffsetDateTime createdAt,
      ReportMeta report,
      /** 진단 이미지 공개 URL (DiagnosisResult.imageUrl). 프론트 리포트 페이지에서 "수리 전 사진" 자동 반영용 */
      String imageUrl
  ) {}

  public record BulkDeleteRequest(@NotEmpty List<Long> ids) {}
}
