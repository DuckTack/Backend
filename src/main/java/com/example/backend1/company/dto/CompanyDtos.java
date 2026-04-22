package com.example.backend1.company.dto;

import com.example.backend1.diagnosis.domain.IssueType;

import java.util.Set;

public class CompanyDtos {

    /** 일반 사용자용 업체 목록 항목 */
    public record CompanyListItem(
            Long id,
            String name,
            String phone,
            String addressLine,
            String serviceRegionLabel,
            Set<IssueType> specialties,
            Integer minEstimatedQuoteKrw,
            Integer maxEstimatedQuoteKrw
    ) {}

    /** 일반 사용자용 업체 상세 */
    public record CompanyDetail(
            Long id,
            String name,
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
            String capabilityNote
    ) {}
}
