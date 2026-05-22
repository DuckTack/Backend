package com.example.backend1.review.dto;

import java.time.OffsetDateTime;
import java.util.List;

public class ReviewDtos {

    /**
     * 리뷰 작성 요청.
     * - DB 제휴 업체: companyId만 필수
     * - 카카오 API 업체: kakaoPlaceId + kakaoPlaceName 필수 (최초 리뷰 시 업체 레코드 lazy 생성)
     */
    public record CreateRequest(
            Long companyId,
            String kakaoPlaceId,
            String kakaoPlaceName,
            String kakaoPlacePhone,
            String kakaoPlaceAddress,
            Double kakaoPlaceLat,
            Double kakaoPlaceLng,
            int rating,      // 1~5
            String content   // nullable
    ) {}

    public record ReviewItem(
            Long id,
            String authorUsername,
            int rating,
            String content,
            OffsetDateTime createdAt
    ) {}

    public record ReviewSummary(
            double avgRating,
            int reviewCount,
            List<ReviewItem> reviews
    ) {}

    /** findNearby batch 조회용 내부 집계 결과 */
    public record ReviewStats(
            double avgRating,
            int reviewCount
    ) {}
}
