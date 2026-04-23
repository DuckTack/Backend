package com.example.backend1.company.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.common.service.DistanceService; // 거리 계산 서비스
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.dto.CompanyDtos;
import com.example.backend1.company.repo.CompanyRepository;
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
 * 제휴 업체(DB)와 일반 업체(네이버 API)를 통합하여 거리순으로 제공한다.
 */
@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);
    private static final int INITIAL_RADIUS_METERS = 10_000;
    private static final int EXPANDED_RADIUS_METERS = 20_000;
    private static final int MAX_EXTERNAL_RESULTS = 12;

    private final CompanyRepository companyRepository;
    private final DistanceService distanceService;
    // NaverSearchClient 는 Geocoding 등 다른 용도로도 쓸 수 있어 필드는 유지.
    // 다만 '주변 업체 찾기(findNearby)' 는 카카오 로컬 API 로 이관했다.
    private final NaverSearchClient naverSearchClient;
    private final KakaoLocalClient kakaoLocalClient;

    public CompanyService(CompanyRepository companyRepository,
                          DistanceService distanceService,
                          NaverSearchClient naverSearchClient,
                          KakaoLocalClient kakaoLocalClient) {
        this.companyRepository = companyRepository;
        this.distanceService = distanceService;
        this.naverSearchClient = naverSearchClient;
        this.kakaoLocalClient = kakaoLocalClient;
    }

    /** 기존 기능: 활성 업체 페이징 조회 */
    @Transactional(readOnly = true)
    public Page<CompanyDtos.CompanyListItem> listActive(Pageable pageable) {
        return companyRepository.findByActive(true, pageable).map(this::toListItem);
    }

    /** 기존 기능: 업체 상세 조회 */
    @Transactional(readOnly = true)
    public CompanyDtos.CompanyDetail getActive(Long id) {
        Company c = companyRepository.findById(id)
                .filter(Company::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND)); //
        return toDetail(c);
    }

    /**
     * ⭐ 수정 및 강화: 내 주변 업체 찾기 (GPS 기반)
     * 1. DB에서 제휴 업체를 가져와 거리를 계산한다.
     * 2. 네이버 API를 통해 주변 일반 업체를 검색한다.
     * 3. 제휴 업체 우선 -> 거리순으로 정렬하여 반환한다.
     */
    @Transactional(readOnly = true)
    public List<CompanyDtos.NearbyCompanyResponse> findNearby(double userLat, double userLon, String keyword) {

        List<CompanyDtos.NearbyCompanyResponse> combinedResults = new ArrayList<>();
        Set<String> partnerNameKeys = new HashSet<>(); // 네이버 결과 중 제휴 업체와 이름 중복되는 항목 제거용

        // ─── 1. DB 내 제휴 업체(Partners) ───────────────────────────────────
        List<Company> partners;
        try {
            partners = companyRepository.findByActive(true, Pageable.unpaged()).getContent();
        } catch (Exception e) {
            log.warn("[findNearby] DB partner 조회 실패, 빈 리스트로 대체. cause={}", e.toString());
            partners = Collections.emptyList();
        }
        int partnerCount = 0;
        int partnerNoGeo = 0;
        for (Company c : partners) {
            if (c == null) continue;

            Double lat = c.getLatitude();
            Double lng = c.getLongitude();
            Double distanceKm = null; // 좌표 없으면 거리 계산 불가 → null 로 그대로 노출

            if (lat != null && lng != null) {
                double d = distanceService.calculate(userLat, userLon, lat, lng);
                distanceKm = Math.round(d * 100) / 100.0;
            } else {
                partnerNoGeo++;
            }

            combinedResults.add(new CompanyDtos.NearbyCompanyResponse(
                    c.getId(),
                    c.getName(),
                    c.getPhone(),
                    c.getAddressLine(),
                    lat,
                    lng,
                    distanceKm,
                    true // 제휴 업체 표시
            ));
            if (c.getName() != null) partnerNameKeys.add(normalizeName(c.getName()));
            partnerCount++;
        }

        // ─── 2. 카카오 로컬 키워드 검색 (일반 업체) ──────────────────────────
        //
        // 네이버 Open API 지역검색(display<=5, 얇은 POI DB)의 한계를 걷어내기 위해
        // 카카오 로컬 API 로 이관했다. 반환 필드가 달라서 아래 매핑을 따른다:
        //
        //   네이버        →  카카오
        //   items         →  documents
        //   title(HTML)   →  place_name (태그 없음, replaceAll 불필요)
        //   mapx/mapy     →  x/y (문자열 소수, 그대로 Double.parseDouble)
        //   telephone     →  phone
        //   roadAddress   →  road_address_name
        //   address       →  address_name
        //   (없음)        →  distance (사용자 x,y 기준 미터, 문자열)
        int externalCount = 0;
        int externalSkippedDup = 0;
        int externalSkippedNoGeo = 0;
        List<CompanyDtos.NearbyCompanyResponse> externalResults = new ArrayList<>();
        Set<String> externalDedupKeys = new HashSet<>();
        try {
            // 10km -> 필요시 20km로 단계 확장. 결과는 상한을 두어 과다 노출 방지.
            List<String> keywordCandidates = buildKeywordCandidates(keyword);
            int[] radiuses = new int[]{INITIAL_RADIUS_METERS, EXPANDED_RADIUS_METERS};

            for (int radius : radiuses) {
                for (String candidate : keywordCandidates) {
                    if (externalResults.size() >= MAX_EXTERNAL_RESULTS) break;
                    Map<String, Object> kakaoData =
                            kakaoLocalClient.searchKeyword(candidate, userLat, userLon, radius);
                    Object rawDocs = (kakaoData == null) ? null : kakaoData.get("documents");
                    if (!(rawDocs instanceof List<?> docList)) continue;

                    for (Object rawDoc : docList) {
                        if (externalResults.size() >= MAX_EXTERNAL_RESULTS) break;
                        if (!(rawDoc instanceof Map<?, ?> m)) continue;

                        // 좌표 — 카카오는 x=경도, y=위도.
                        Object xObj = m.get("x");
                        Object yObj = m.get("y");
                        if (xObj == null || yObj == null) {
                            externalSkippedNoGeo++;
                            continue;
                        }

                        double lat;
                        double lng;
                        try {
                            lat = Double.parseDouble(String.valueOf(yObj));
                            lng = Double.parseDouble(String.valueOf(xObj));
                        } catch (NumberFormatException e) {
                            externalSkippedNoGeo++;
                            continue;
                        }

                        Object nameObj = m.get("place_name");
                        String cleanName = (nameObj == null) ? "" : String.valueOf(nameObj).trim();

                        // 제휴 업체와 이름 중복되면 스킵 (제휴 우선 노출)
                        if (!cleanName.isBlank() && partnerNameKeys.contains(normalizeName(cleanName))) {
                            externalSkippedDup++;
                            continue;
                        }

                        // 외부 업체끼리 중복 제거 (이름+좌표 key)
                        String dedupKey = normalizeName(cleanName) + "#" + String.format(Locale.ROOT, "%.5f,%.5f", lat, lng);
                        if (!externalDedupKeys.add(dedupKey)) {
                            externalSkippedDup++;
                            continue;
                        }

                        double dist;
                        Object distObj = m.get("distance");
                        if (distObj != null && !String.valueOf(distObj).isBlank()) {
                            try {
                                dist = Double.parseDouble(String.valueOf(distObj)) / 1000.0;
                            } catch (NumberFormatException e) {
                                dist = distanceService.calculate(userLat, userLon, lat, lng);
                            }
                        } else {
                            dist = distanceService.calculate(userLat, userLon, lat, lng);
                        }

                        Object phoneObj = m.get("phone");
                        Object roadObj = m.get("road_address_name");
                        Object addrFallback = m.get("address_name");
                        String address = (roadObj != null && !String.valueOf(roadObj).isBlank())
                                ? String.valueOf(roadObj)
                                : (addrFallback == null ? null : String.valueOf(addrFallback));

                        externalResults.add(new CompanyDtos.NearbyCompanyResponse(
                                null,
                                cleanName,
                                phoneObj == null ? null : String.valueOf(phoneObj),
                                address,
                                lat,
                                lng,
                                Math.round(dist * 100) / 100.0,
                                false
                        ));
                        externalCount++;
                    }
                }
                if (externalResults.size() >= MAX_EXTERNAL_RESULTS) break;
            }
        } catch (Exception e) {
            // 카카오 호출 실패해도 제휴 업체 목록은 그대로 내보낸다.
            log.warn("[findNearby] 카카오 검색 실패, 제휴 업체만 반환. keyword='{}', cause={}",
                    keyword, e.toString());
        }

        combinedResults.addAll(externalResults);

        log.info("[findNearby] keyword='{}' partners={} (noGeo={}) kakao={} (skipDup={}, skipNoGeo={})",
                keyword, partnerCount, partnerNoGeo, externalCount, externalSkippedDup, externalSkippedNoGeo);

        // ─── 3. 정렬: 제휴 우선 → 거리순 (null 거리는 그룹 끝으로) ─────────
        return combinedResults.stream()
                .sorted(Comparator
                        .comparing(CompanyDtos.NearbyCompanyResponse::isPartner).reversed()
                        .thenComparing(CompanyDtos.NearbyCompanyResponse::distanceKm,
                                Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());
    }

    private List<String> buildKeywordCandidates(String keyword) {
        String base = (keyword == null || keyword.isBlank()) ? "집수리" : keyword.trim();
        LinkedHashSet<String> candidates = new LinkedHashSet<>();
        candidates.add(base);

        String[] tokens = base.split("\\s+");
        if (tokens.length >= 2) {
            candidates.add(tokens[tokens.length - 1]); // ex) "경기 누수수리" -> "누수수리"
        }

        if (base.contains("누수")) {
            candidates.add("누수탐지");
            candidates.add("배관");
            candidates.add("설비");
        } else if (base.contains("곰팡이")) {
            candidates.add("곰팡이제거");
            candidates.add("방수");
            candidates.add("집수리");
        } else if (base.contains("전기")) {
            candidates.add("전기공사");
            candidates.add("전기");
        } else {
            candidates.add("집수리");
        }
        return new ArrayList<>(candidates);
    }

    /** 업체명 중복 체크용 정규화: 공백/특수문자 제거 + 소문자화. */
    private String normalizeName(String s) {
        if (s == null) return "";
        return s.replaceAll("[\\s\\p{Punct}]+", "").toLowerCase(Locale.ROOT);
    }

    // ─── Mappers (기존 유지) ──────────────────────────────────────────────────────────

    private CompanyDtos.CompanyListItem toListItem(Company c) {
        return new CompanyDtos.CompanyListItem(
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getAddressLine(),
                c.getServiceRegionLabel(),
                new HashSet<>(c.getSpecialties()), //
                c.getMinEstimatedQuoteKrw(),
                c.getMaxEstimatedQuoteKrw()
        );
    }

    private CompanyDtos.CompanyDetail toDetail(Company c) {
        return new CompanyDtos.CompanyDetail(
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getEmail(),
                c.getAddressLine(),
                c.getPostalCode(),
                c.getServiceRegionLabel(),
                c.getLatitude(),
                c.getLongitude(),
                new HashSet<>(c.getSpecialties()),
                c.getMinEstimatedQuoteKrw(),
                c.getMaxEstimatedQuoteKrw(),
                c.getCapabilityNote()
        );
    }
}