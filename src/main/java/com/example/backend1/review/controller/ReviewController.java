package com.example.backend1.review.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.review.dto.ReviewDtos;
import com.example.backend1.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Review")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * 리뷰 작성 (로그인 필요)
     * POST /api/reviews
     *
     * - DB 제휴 업체: { companyId, rating, content }
     * - 카카오 업체:  { kakaoPlaceId, kakaoPlaceName, rating, content, ... }
     */
    @Operation(summary = "리뷰 작성")
    @PostMapping
    public ApiResponse<ReviewDtos.ReviewItem> create(
            @RequestBody ReviewDtos.CreateRequest req,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        return ApiResponse.ok(reviewService.createReview(req, userDetails.getUsername()));
    }

    /**
     * 업체 리뷰 조회 (로그인 불필요)
     * GET /api/reviews?companyId=1
     * GET /api/reviews?kakaoPlaceId=xxx
     */
    @Operation(summary = "업체 리뷰 조회")
    @GetMapping
    public ApiResponse<ReviewDtos.ReviewSummary> get(
            @RequestParam(required = false) Long companyId,
            @RequestParam(required = false) String kakaoPlaceId
    ) {
        if (companyId != null) {
            return ApiResponse.ok(reviewService.getByCompanyId(companyId));
        }
        if (kakaoPlaceId != null) {
            return ApiResponse.ok(reviewService.getByKakaoPlaceId(kakaoPlaceId));
        }
        return ApiResponse.ok(new ReviewDtos.ReviewSummary(0.0, 0, java.util.Collections.emptyList()));
    }
}
