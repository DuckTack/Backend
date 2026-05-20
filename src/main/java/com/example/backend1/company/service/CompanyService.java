package com.example.backend1.company.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.common.service.DistanceService;
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.dto.CompanyDtos;
import com.example.backend1.company.repo.CompanyRepository;
import com.example.backend1.review.dto.ReviewDtos;
import com.example.backend1.review.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 일반 사용자용 업체 서비스.
 * 제휴 업체(DB)와 일반 업체(카카오 API)를 통합하여 거리순으로 제공한다.
 */
@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private static final int INITIAL_RADIUS_METERS = 10_000;
    private static final int EXPANDED_RADIUS_METERS = 20_000;
    private static final int MAX_EXTERNAL_RESULTS = 12;

    private final CompanyRepository companyRepository;
    private final DistanceService distanceService;
    private final NaverSearchClient naverSearchClient;
    private final KakaoLocalClient kakaoLocalClient;
    private final ReviewService reviewService;

    public CompanyService(CompanyRepository companyRepository,
                          DistanceService distanceService,
                          NaverSearchClient naverSearchClient,
                          KakaoLocalClient kakaoLocalClient,
                          ReviewService reviewService) {
        this.companyRepository = companyRepository;
        this.distanceService = distanceService;
        this.naverSearchClient = naverSearchClient;
        this.kakaoLocalClient = kakaoLocalClient;
        this.reviewService = reviewService;
    }

    @Transactional(readOnly = true)
    public Page<CompanyDtos.CompanyListItem> listActive(Pageable pageable) {
        return companyRepository.findByActive(true, pageable).map(this::toListItem);
    }

    @Transactional(readOnly = true)
    public CompanyDtos.CompanyDetail getActive(Long id) {
        Company c = companyRepository.findById(id)
                .filter(Company::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
        return toDetail(c);
    }

    /**
     * 내 주변 업체 찾기 (GPS 기반)
     * 1. DB 제휴 업체 + 거리 계산
     * 2. 카카오 로컬 키워드 검색
     * 3. 두 결과에 대해 리뷰 통계 batch 조회 후 attach
     * 4. 제휴 업체 우선 → 거리순 정렬
     */
    @Transactional(readOnly = true)
    public List<CompanyDtos.NearbyCompanyResponse> findNearby(double userLat, double userLon, String keyword) {

        List<CompanyDtos.NearbyCompanyResponse> combined = new ArrayList<>();
        Set<String> partnerNameKeys = new HashSet<>();

        // ─── 1. DB 제휴 업체 ────────────────────────────────────────────────────
        List<Company> partners;
        try {
            partners = companyRepository.findByActive(true, Pageable.unpaged()).getContent();
        } catch (Exception e) {
            log.warn("[findNearby] DB partner 조회 실패, 빈 리스트로 대체. cause={}", e.toString());
            partners = Collections.emptyList();
        }

        int partnerCount = 0, partnerNoGeo = 0;
        for (Company c : partners) {
            if (c == null) continue;
            Double lat = c.getLatitude();
            Double lng = c.getLongitude();
            Double distanceKm = null;
            if (lat != null && lng != null) {
                double d = distanceService.calculate(userLat, userLon, lat, lng);
                distanceKm = Math.round(d * 100) / 100.0;
            } else {
                partnerNoGeo++;
            }
            combined.add(new CompanyDtos.NearbyCompanyResponse(
                    c.getId(), c.getName(), c.getPhone(), c.getAddressLine(),
                    lat, lng, distanceKm, true,
                    null, null, 0   // 리뷰 통계는 후처리에서 채운다
            ));
            if (c.getName() != null) partnerNameKeys.add(normalizeName(c.getName()));
            partnerCount++;
        }

        // ─── 2. 카카오 로컬 키워드 검색 ─────────────────────────────────────────
        int externalCount = 0, externalSkippedDup = 0, externalSkippedNoGeo = 0;
        List<CompanyDtos.NearbyCompanyResponse> externalResults = new ArrayList<>();
        Set<String> externalDedupKeys = new HashSet<>();
        try {
            List<String> keywordCandidates = buildKeywordCandidates(keyword);
            int[] radiuses = {INITIAL_RADIUS_METERS, EXPANDED_RADIUS_METERS};

            outer:
            for (int radius : radiuses) {
                for (String candidate : keywordCandidates) {
                    if (externalResults.size() >= MAX_EXTERNAL_RESULTS) break outer;
                    Map<String, Object> kakaoData =
                            kakaoLocalClient.searchKeyword(candidate, userLat, userLon, radius);
                    Object rawDocs = (kakaoData == null) ? null : kakaoData.get("documents");
                    if (!(rawDocs instanceof List<?> docList)) continue;

                    for (Object rawDoc : docList) {
                        if (externalResults.size() >= MAX_EXTERNAL_RESULTS) break;
                        if (!(rawDoc instanceof Map<?, ?> m)) continue;

                        Object xObj = m.get("x"), yObj = m.get("y");
                        if (xObj == null || yObj == null) { externalSkippedNoGeo++; continue; }

                        double lat, lng;
                        try {
                            lat = Double.parseDouble(String.valueOf(yObj));
                            lng = Double.parseDouble(String.valueOf(xObj));
                        } catch (NumberFormatException e) { externalSkippedNoGeo++; continue; }

                        Object nameObj = m.get("place_name");
                        String cleanName = (nameObj == null) ? "" : String.valueOf(nameObj).trim();

                        if (!cleanName.isBlank() && partnerNameKeys.contains(normalizeName(cleanName))) {
                            externalSkippedDup++; continue;
                        }

                        String dedupKey = normalizeName(cleanName)
                                + "#" + String.format(Locale.ROOT, "%.5f,%.5f", lat, lng);
                        if (!externalDedupKeys.add(dedupKey)) { externalSkippedDup++; continue; }

                        double dist;
                        Object distObj = m.get("distance");
                        if (distObj != null && !String.valueOf(distObj).isBlank()) {
                            try { dist = Double.parseDouble(String.valueOf(distObj)) / 1000.0; }
                            catch (NumberFormatException e) { dist = distanceService.calculate(userLat, userLon, lat, lng); }
                        } else {
                            dist = distanceService.calculate(userLat, userLon, lat, lng);
                        }

                        Object phoneObj = m.get("phone");
                        Object roadObj = m.get("road_address_name");
                        Object addrFallback = m.get("address_name");
                        String address = (roadObj != null && !String.valueOf(roadObj).isBlank())
                                ? String.valueOf(roadObj)
                                : (addrFallback == null ? null : String.valueOf(addrFallback));

                        // 카카오 place.id 파싱
                        Object placeIdObj = m.get("id");
                        String kakaoPlaceId = (placeIdObj == null) ? null : String.valueOf(placeIdObj).trim();

                        externalResults.add(new CompanyDtos.NearbyCompanyResponse(
                                null, cleanName,
                                phoneObj == null ? null : String.valueOf(phoneObj),
                                address, lat, lng,
                                Math.round(dist * 100) / 100.0,
                                false,
                                kakaoPlaceId, null, 0  // 리뷰 통계는 후처리에서 채운다
                        ));
                        externalCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[findNearby] 카카오 검색 실패, 제휴 업체만 반환. keyword='{}', cause={}", keyword, e.toString());
        }

        combined.addAll(externalResults);

        log.info("[findNearby] keyword='{}' partners={} (noGeo={}) kakao={} (skipDup={}, skipNoGeo={})",
                keyword, partnerCount, partnerNoGeo, externalCount, externalSkippedDup, externalSkippedNoGeo);

        // ─── 3. 리뷰 통계 batch 조회 후 attach ──────────────────────────────────
        combined = attachReviewStats(combined);

        // ─── 4. 제휴 업체 우선 → 거리순 ────────────────────────────────────────
        return combined.stream()
                .sorted(Comparator
                        .comparing(CompanyDtos.NearbyCompanyResponse::partner).reversed()
                        .thenComparing(CompanyDtos.NearbyCompanyResponse::distanceKm,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    // ─── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private List<CompanyDtos.NearbyCompanyResponse> attachReviewStats(
            List<CompanyDtos.NearbyCompanyResponse> list) {

        List<Long> partnerIds = list.stream()
                .filter(r -> r.id() != null)
                .map(CompanyDtos.NearbyCompanyResponse::id)
                .collect(Collectors.toList());

        List<String> kakaoIds = list.stream()
                .filter(r -> r.kakaoPlaceId() != null)
                .map(CompanyDtos.NearbyCompanyResponse::kakaoPlaceId)
                .collect(Collectors.toList());

        Map<Long, ReviewDtos.ReviewStats> partnerStats = reviewService.getSummaryByCompanyIds(partnerIds);
        Map<String, ReviewDtos.ReviewStats> kakaoStats = reviewService.getSummaryByKakaoPlaceIds(kakaoIds);

        return list.stream().map(r -> {
            ReviewDtos.ReviewStats stats = (r.id() != null)
                    ? partnerStats.get(r.id())
                    : kakaoStats.get(r.kakaoPlaceId());
            if (stats == null) return r;
            return new CompanyDtos.NearbyCompanyResponse(
                    r.id(), r.name(), r.phone(), r.address(),
                    r.latitude(), r.longitude(), r.distanceKm(), r.partner(),
                    r.kakaoPlaceId(), stats.avgRating(), stats.reviewCount()
            );
        }).collect(Collectors.toList());
    }

    private List<String> buildKeywordCandidates(String keyword) {
        String base = (keyword == null || keyword.isBlank()) ? "집수리" : keyword.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(base);

        String[] tokens = base.split("\\s+");
        if (tokens.length >= 2) candidates.add(tokens[tokens.length - 1]);

        if (base.contains("누수")) {
            candidates.add("누수탐지"); candidates.add("배관"); candidates.add("설비");
        } else if (base.contains("곰팡이")) {
            candidates.add("곰팡이제거"); candidates.add("방수"); candidates.add("집수리");
        } else if (base.contains("전기")) {
            candidates.add("전기공사"); candidates.add("전기");
        } else {
            candidates.add("집수리");
        }
        return new ArrayList<>(candidates);
    }

    private String normalizeName(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\p{Punct}]+", "").toLowerCase(Locale.ROOT);
    }

    private CompanyDtos.CompanyListItem toListItem(Company c) {
        return new CompanyDtos.CompanyListItem(
                c.getId(), c.getName(), c.getPhone(), c.getAddressLine(),
                c.getServiceRegionLabel(), new HashSet<>(c.getSpecialties()),
                c.getMinEstimatedQuoteKrw(), c.getMaxEstimatedQuoteKrw()
        );
    }

    private CompanyDtos.CompanyDetail toDetail(Company c) {
        return new CompanyDtos.CompanyDetail(
                c.getId(), c.getName(), c.getPhone(), c.getEmail(),
                c.getAddressLine(), c.getPostalCode(), c.getServiceRegionLabel(),
                c.getLatitude(), c.getLongitude(), new HashSet<>(c.getSpecialties()),
                c.getMinEstimatedQuoteKrw(), c.getMaxEstimatedQuoteKrw(), c.getCapabilityNote()
        );
    }
}
