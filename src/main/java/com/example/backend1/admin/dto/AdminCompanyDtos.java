package com.example.backend1.admin.dto;

import com.example.backend1.diagnosis.domain.IssueType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.Set;

public class AdminCompanyDtos {

    public record CreateRequest(
            @NotBlank @Size(max = 200) String name,
            @Size(max = 20) String businessRegistrationNumber,
            @Size(max = 100) String representativeName,
            @Size(max = 30) String phone,
            @Size(max = 255) String email,
            @Size(max = 500) String addressLine,
            @Size(max = 20) String postalCode,
            @Size(max = 200) String serviceRegionLabel,
            Double latitude,
            Double longitude,
            Set<IssueType> specialties,
            Integer minEstimatedQuoteKrw,
            Integer maxEstimatedQuoteKrw,
            @Size(max = 2000) String capabilityNote,
            Boolean active,
            @Size(max = 2000) String adminMemo
    ) {}

    public record UpdateRequest(
            @Size(max = 200) String name,
            @Size(max = 20) String businessRegistrationNumber,
            @Size(max = 100) String representativeName,
            @Size(max = 30) String phone,
            @Size(max = 255) String email,
            @Size(max = 500) String addressLine,
            @Size(max = 20) String postalCode,
            @Size(max = 200) String serviceRegionLabel,
            Double latitude,
            Double longitude,
            Set<IssueType> specialties,
            Integer minEstimatedQuoteKrw,
            Integer maxEstimatedQuoteKrw,
            @Size(max = 2000) String capabilityNote,
            Boolean active,
            @Size(max = 2000) String adminMemo
    ) {}

    /** 목록용: 상세는 {@link CompanyResponse} + GET /api/admin/companies/{id} */
    public record CompanyListItem(
            Long id,
            String name,
            /** 우편번호 + 상세주소를 한 줄로 합친 표시용(없으면 null) */
            String address,
            /** 숨김/노출 상태 — 프론트에서 상세 API 추가 호출 없이 바로 표시 가능 */
            boolean active
    ) {}

    public record CompanyResponse(
            Long id,
            String name,
            String businessRegistrationNumber,
            String representativeName,
            String phone,
            String email,
            String addressLine,
            String postalCode,
            String serviceRegionLabel,
            Double latitude,
            Double longitude,
            Set<IssueType> specialties,
            Integer minEstimatedQuoteKrw,
            Integer maxEstimatedQuoteKrw,
            String capabilityNote,
            boolean active,
            String adminMemo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {}

    public record SetActiveRequest(boolean active) {}
}