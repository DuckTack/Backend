package com.example.backend1.expert.dto;

import java.util.List;

/**
 * 프론트엔드 `ExpertVendor` 스키마와 맞춘 응답 DTO.
 * - id, name, phone, addressLine, serviceRegionLabel: 실제 DB 필드
 * - minPrice, maxPrice: minEstimatedQuoteKrw / maxEstimatedQuoteKrw 매핑
 * - intro: capabilityNote 대체
 * - coverageAreas: serviceRegionLabel 파싱
 * - rating, reviewCount: 현재 DB 컬럼이 없어 일단 합성값(결정론적 해시 기반)으로 제공.
 *   추후 Company 엔티티에 실제 컬럼이 추가되면 이 DTO만 그대로 두고 service 쪽만 교체하면 됨.
 */
public class ExpertDtos {

    public record ExpertVendor(
            Long id,
            String name,
            double rating,
            int reviewCount,
            int minPrice,
            Integer maxPrice,
            String phone,
            String intro,
            List<String> coverageAreas,
            String addressLine,
            String serviceRegionLabel,
            Double distanceKm
    ) {}
}
