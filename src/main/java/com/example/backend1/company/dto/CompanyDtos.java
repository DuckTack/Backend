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

    // 기존 CompanyDtos 클래스 내부에 추가
    // ⭐ 좌표/거리 필드를 boxed Double 로 변경: 관리자가 좌표 없이 등록한 제휴 업체도
    //    "거리 정보 없음" 상태로 노출할 수 있도록 null 허용.
    public record NearbyCompanyResponse(
            Long id,               // 제휴 업체인 경우 ID, 아니면 null
            String name,           // 업체명
            String phone,
            String address,
            Double latitude,       // null 가능 (관리자 수동 등록 시 좌표 미입력일 때)
            Double longitude,      // null 가능
            Double distanceKm,     // null 가능 (좌표가 없어 거리 계산 불가한 제휴 업체)
            boolean isPartner      // 제휴 업체 여부 (우선순위용)
    ) {}
}
