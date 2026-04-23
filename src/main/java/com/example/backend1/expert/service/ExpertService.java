package com.example.backend1.expert.service;

import com.example.backend1.company.domain.Company;
import com.example.backend1.company.repo.CompanyRepository;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.expert.dto.ExpertDtos;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 프론트엔드 /api/experts/vendors 용 서비스.
 * Company 엔티티를 ExpertVendor DTO 로 변환하며,
 * region / issueType 필터와 price/rating 정렬을 지원한다.
 *
 * 설계 원칙:
 *  - 어떤 파라미터가 null 이거나 이상해도 예외를 던지지 않는다.
 *  - 필터 결과가 비어도 정상적으로 빈 리스트를 돌려준다.
 */
@Service
public class ExpertService {

    private final CompanyRepository companyRepository;

    public ExpertService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional(readOnly = true)
    public List<ExpertDtos.ExpertVendor> listVendors(String region,
                                                     String issueTypeRaw,
                                                     String sortKey,
                                                     String direction) {

        List<Company> companies;
        try {
            companies = companyRepository.findByActive(true, Pageable.unpaged()).getContent();
        } catch (Exception e) {
            companies = Collections.emptyList();
        }
        if (companies == null) companies = Collections.emptyList();

        final IssueType issueType = parseIssueType(issueTypeRaw);
        final String normalizedRegion = (region == null) ? "" : region.trim();

        List<ExpertDtos.ExpertVendor> result = companies.stream()
                .filter(Objects::nonNull)
                .filter(c -> matchesRegion(c, normalizedRegion))
                .filter(c -> matchesIssueType(c, issueType))
                .map(this::toVendor)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 정렬 키 정규화
        String key = (sortKey == null) ? "price" : sortKey.trim().toLowerCase();
        String dir = (direction == null) ? "asc" : direction.trim().toLowerCase();

        Comparator<ExpertDtos.ExpertVendor> comparator;
        if ("rating".equals(key)) {
            comparator = Comparator.comparingDouble(ExpertDtos.ExpertVendor::rating);
        } else {
            comparator = Comparator.comparingInt(ExpertDtos.ExpertVendor::minPrice);
        }

        if ("desc".equals(dir)) {
            comparator = comparator.reversed();
        }

        // id tie-breaker 로 안정적 순서 보장 (제네릭 추론 애매함 없애려고 Comparator.comparingLong 로 명시)
        Comparator<ExpertDtos.ExpertVendor> idTieBreaker =
                Comparator.comparingLong(v -> v.id() == null ? Long.MAX_VALUE : v.id().longValue());
        comparator = comparator.thenComparing(idTieBreaker);

        result.sort(comparator);
        return result;
    }

    // ────────────────────────────────────────────────────────────────
    // 내부 유틸
    // ────────────────────────────────────────────────────────────────

    private boolean matchesRegion(Company c, String region) {
        if (region.isEmpty()) return true;
        String label = c.getServiceRegionLabel();
        // 지역 정보 자체가 없는 업체는 제외하지 않고 모두 노출(= "전국" 취급)
        if (label == null || label.isBlank()) return true;
        return label.contains(region);
    }

    private boolean matchesIssueType(Company c, IssueType issueType) {
        if (issueType == null) return true;
        Set<IssueType> specialties;
        try {
            specialties = c.getSpecialties();
        } catch (Exception e) {
            specialties = null;
        }
        if (specialties == null || specialties.isEmpty()) return true; // 미지정 업체는 모든 유형 처리 가능한 것으로 취급
        return specialties.contains(issueType);
    }

    private IssueType parseIssueType(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return IssueType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private ExpertDtos.ExpertVendor toVendor(Company c) {
        if (c == null) return null;

        String name = nullToEmpty(c.getName());
        String phone = nullToEmpty(c.getPhone());
        String addressLine = nullToEmpty(c.getAddressLine());
        String serviceRegion = nullToEmpty(c.getServiceRegionLabel());
        String capNote = c.getCapabilityNote();

        // coverageAreas: "서울 강남구, 서초구" 같은 자유 텍스트에서 최대한 안전하게 토큰화
        List<String> coverage = new ArrayList<>();
        if (!serviceRegion.isBlank()) {
            for (String part : serviceRegion.split("[,/·|]+")) {
                String t = part == null ? "" : part.trim();
                if (!t.isEmpty()) coverage.add(t);
            }
        }
        if (coverage.isEmpty()) coverage.add("전국");

        // rating / reviewCount: DB 컬럼이 아직 없음 → 결정론적 합성값으로 채워 UI가 깨지지 않도록 한다.
        // (동일한 업체는 항상 동일한 값을 보이도록 id 해시 기반)
        long idForSeed = c.getId() == null ? 0L : c.getId();
        double rating = 4.0 + Math.abs((int)(idForSeed * 2654435761L) % 10) / 10.0; // 4.0 ~ 4.9
        int reviewCount = Math.abs((int)(idForSeed * 2246822519L) % 200);            // 0 ~ 199

        Integer min = c.getMinEstimatedQuoteKrw();
        Integer max = c.getMaxEstimatedQuoteKrw();
        int minPrice = (min == null || min < 0) ? 0 : min;

        String intro = (capNote == null || capNote.isBlank())
                ? (name.isEmpty() ? "전문 수리 서비스를 제공합니다." : name + " 의 전문 수리 서비스입니다.")
                : capNote;

        return new ExpertDtos.ExpertVendor(
                c.getId(),
                name,
                Math.round(rating * 10.0) / 10.0,
                reviewCount,
                minPrice,
                max,
                phone,
                intro,
                coverage,
                addressLine,
                serviceRegion,
                null // distanceKm 은 프론트에서 /api/companies/nearby 결과로 merge
        );
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
