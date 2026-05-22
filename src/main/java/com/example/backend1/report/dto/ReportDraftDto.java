package com.example.backend1.report.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

public class ReportDraftDto {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DraftRequest(
            String repairMethod,

            // ⭐ 프론트 기준
            String repairDate,
            String contractorName,
            String contractorContact,
            String repairSummary,
            Integer actualCostKrw,
            String notes,

            // 기존 추가 필드
            String materialCost,
            String laborCost,
            String totalCost,
            String diyMaterialsUsed,
            String diyMaterialCost,
            String diyWorkMemo,


            // ⭐ 핵심 수정 (프론트: beforeImageKeys 대응)
            @JsonAlias("beforeImageKeys")
            List<String> beforeImageUris,

            @JsonAlias("afterImageKeys")
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
            String totalCost,
            String diyMaterialsUsed,
            String diyMaterialCost,
            String diyWorkMemo,

            // ⭐ 응답은 그대로 (프론트는 이걸로 받음)
            List<String> beforeImageUris,
            List<String> afterImageUris,

            LocalDateTime updatedAt
    ) {}
}