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
            Long id,               // 제휴 업체인 경우 DB PK, 아니면 null
            String name,
            String phone,
            String address,
            Double latitude,
            Double longitude,
            Double distanceKm,
            boolean isPartner,     // 제휴 업체 여부 (우선순위용)
            String kakaoPlaceId,   // 카카오 place.id (카카오 업체일 때만 존재)
            Double avgRating,      // null = 리뷰 없음
            Integer reviewCount
    ) {}
}
