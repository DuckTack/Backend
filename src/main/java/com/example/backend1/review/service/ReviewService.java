package com.example.backend1.review.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.repo.CompanyRepository;
import com.example.backend1.review.domain.Review;
import com.example.backend1.review.dto.ReviewDtos;
import com.example.backend1.review.repo.ReviewRepository;
import com.example.backend1.user.domain.User;
import com.example.backend1.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         CompanyRepository companyRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewDtos.ReviewItem createReview(ReviewDtos.CreateRequest req, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Company company = resolveCompany(req);

        if (reviewRepository.existsByCompanyAndUser(company, user)) {
            throw new ApiException(ErrorCode.REVIEW_DUPLICATE);
        }

        int rating = Math.min(5, Math.max(1, req.rating()));
        Review review = new Review(company, user, rating, req.content());
        reviewRepository.save(review);

        return toItem(review);
    }

    @Transactional(readOnly = true)
    public ReviewDtos.ReviewSummary getByCompanyId(Long companyId) {
        if (!companyRepository.existsById(companyId)) {
            throw new ApiException(ErrorCode.COMPANY_NOT_FOUND);
        }
        List<Review> reviews = reviewRepository.findByCompanyIdOrderByCreatedAtDesc(companyId);
        return toSummary(reviews);
    }

    @Transactional(readOnly = true)
    public ReviewDtos.ReviewSummary getByKakaoPlaceId(String kakaoPlaceId) {
        Optional<Company> company = companyRepository.findByKakaoPlaceId(kakaoPlaceId);
        if (company.isEmpty()) {
            return new ReviewDtos.ReviewSummary(0.0, 0, Collections.emptyList());
        }
        List<Review> reviews = reviewRepository.findByCompanyIdOrderByCreatedAtDesc(company.get().getId());
        return toSummary(reviews);
    }

    /** findNearby 전용 batch 조회: companyId → ReviewStats */
    @Transactional(readOnly = true)
    public Map<Long, ReviewDtos.ReviewStats> getSummaryByCompanyIds(List<Long> companyIds) {
        if (companyIds.isEmpty()) return Collections.emptyMap();
        List<Object[]> rows = reviewRepository.aggregateByCompanyIds(companyIds);
        Map<Long, ReviewDtos.ReviewStats> result = new HashMap<>();
        for (Object[] row : rows) {
            Long cid = ((Number) row[0]).longValue();
            double avg = ((Number) row[1]).doubleValue();
            int cnt = ((Number) row[2]).intValue();
            result.put(cid, new ReviewDtos.ReviewStats(Math.round(avg * 10.0) / 10.0, cnt));
        }
        return result;
    }

    /** findNearby 전용 batch 조회: kakaoPlaceId → ReviewStats */
    @Transactional(readOnly = true)
    public Map<String, ReviewDtos.ReviewStats> getSummaryByKakaoPlaceIds(List<String> kakaoPlaceIds) {
        if (kakaoPlaceIds.isEmpty()) return Collections.emptyMap();

        List<Company> companies = companyRepository.findByKakaoPlaceIdIn(kakaoPlaceIds);
        if (companies.isEmpty()) return Collections.emptyMap();

        Map<Long, String> companyIdToKakaoId = companies.stream()
                .collect(Collectors.toMap(Company::getId, Company::getKakaoPlaceId));

        List<Long> companyIds = new ArrayList<>(companyIdToKakaoId.keySet());
        Map<Long, ReviewDtos.ReviewStats> statsByCompanyId = getSummaryByCompanyIds(companyIds);

        Map<String, ReviewDtos.ReviewStats> result = new HashMap<>();
        for (Map.Entry<Long, String> e : companyIdToKakaoId.entrySet()) {
            ReviewDtos.ReviewStats stats = statsByCompanyId.get(e.getKey());
            if (stats != null) {
                result.put(e.getValue(), stats);
            }
        }
        return result;
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────────

    private Company resolveCompany(ReviewDtos.CreateRequest req) {
        if (req.companyId() != null) {
            return companyRepository.findById(req.companyId())
                    .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
        }
        if (req.kakaoPlaceId() != null && req.kakaoPlaceName() != null) {
            Optional<Company> existing = companyRepository.findByKakaoPlaceId(req.kakaoPlaceId());
            if (existing.isPresent()) {
                Company c = existing.get();
                // 주소/좌표/전화번호가 비어있으면 이번 요청 데이터로 채운다
                boolean needsUpdate = false;
                if ((c.getAddressLine() == null || c.getAddressLine().isBlank())
                        && req.kakaoPlaceAddress() != null) {
                    needsUpdate = true;
                }
                if (c.getLatitude() == null && req.kakaoPlaceLat() != null) needsUpdate = true;
                if (c.getLongitude() == null && req.kakaoPlaceLng() != null) needsUpdate = true;
                if (needsUpdate) {
                    c.updateFromKakao(
                            req.kakaoPlacePhone(),
                            req.kakaoPlaceAddress(),
                            req.kakaoPlaceLat(),
                            req.kakaoPlaceLng()
                    );
                    companyRepository.save(c);
                }
                return c;
            }
            return companyRepository.save(
                    Company.fromKakao(
                            req.kakaoPlaceId(),
                            req.kakaoPlaceName(),
                            req.kakaoPlacePhone(),
                            req.kakaoPlaceAddress(),
                            req.kakaoPlaceLat(),
                            req.kakaoPlaceLng()
                    )
            );
        }
        throw new ApiException(ErrorCode.REVIEW_INVALID_TARGET);
    }

    private ReviewDtos.ReviewSummary toSummary(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return new ReviewDtos.ReviewSummary(0.0, 0, Collections.emptyList());
        }
        double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
        List<ReviewDtos.ReviewItem> items = reviews.stream().map(this::toItem).collect(Collectors.toList());
        return new ReviewDtos.ReviewSummary(Math.round(avg * 10.0) / 10.0, reviews.size(), items);
    }

    private ReviewDtos.ReviewItem toItem(Review r) {
        return new ReviewDtos.ReviewItem(
                r.getId(),
                r.getUser().getUsername(),
                r.getRating(),
                r.getContent(),
                r.getCreatedAt()
        );
    }
}
