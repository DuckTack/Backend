package com.example.backend1.report.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ReportDraftDto {

    public record DraftRequest(
            String repairMethod,

            // ⭐ 프론트 기준으로 맞춤
            String repairDate,
            String contractorName,
            String contractorContact,
            String repairSummary,
            Integer actualCostKrw,
            String notes,

            // 기존 추가 필드
            String materialCost,
            String laborCost,
            String totalCost,          // ⭐ 추가
            String diyMaterialsUsed,
            String diyMaterialCost,
            String diyWorkMemo,

            // ⭐ 이미지 (프론트에 있음)
            List<String> beforeImageUris,
            List<String> afterImageUris
    ) {}

    public record DraftResponse(
            Long diagnosisId,
            String repairMethod,

            // ⭐ 프론트 기준
            String repairDate,
            String contractorName,
            String contractorContact,
            String repairSummary,
            Integer actualCostKrw,
            String notes,

            // 기존
            String materialCost,
            String laborCost,
            String totalCost,          // ⭐ 추가
            String diyMaterialsUsed,
            String diyMaterialCost,
            String diyWorkMemo,

            // ⭐ 이미지
            List<String> beforeImageUris,
            List<String> afterImageUris,

            LocalDateTime updatedAt
    ) {}
}