package com.example.backend1.company.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.common.service.DistanceService; // ⭐ 추가
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.dto.CompanyDtos;
import com.example.backend1.company.dto.CompanyResponse;   // ⭐ 추가
import com.example.backend1.company.repo.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final DistanceService distanceService; // ⭐ 추가

    public CompanyService(CompanyRepository companyRepository,
                          DistanceService distanceService) {
        this.companyRepository = companyRepository;
        this.distanceService = distanceService;
    }

    /** 기존 기능 유지 */
    @Transactional(readOnly = true)
    public Page<CompanyDtos.CompanyListItem> listActive(Pageable pageable) {
        return companyRepository.findByActive(true, pageable).map(this::toListItem);
    }

    /** 기존 기능 유지 */
    @Transactional(readOnly = true)
    public CompanyDtos.CompanyDetail getActive(Long id) {
        Company c = companyRepository.findById(id)
                .filter(Company::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
        return toDetail(c);
    }

    // =========================
    // ⭐ 새로 추가 (핵심)
    // =========================
    @Transactional(readOnly = true)
    public List<CompanyResponse> findNearby(double userLat, double userLon, String region) {

        // active 업체만 가져오기
        List<Company> companies = companyRepository.findByActiveTrueAndServiceRegionLabelContaining(region);

        return companies.stream()
                // 좌표 없는 업체 제외
                .filter(c -> c.getLatitude() != null && c.getLongitude() != null)

                // 거리 계산
                .map(c -> {
                    double distance = distanceService.calculate(
                            userLat,
                            userLon,
                            c.getLatitude(),
                            c.getLongitude()
                    );
                    return new CompanyResponse(c, distance);
                })

                // 거리순 정렬
                .sorted(Comparator.comparing(CompanyResponse::getDistanceKm))

                .toList();
    }

    // ─── Mappers ───────────────────────────────────────────────────────────────

    private CompanyDtos.CompanyListItem toListItem(Company c) {
        return new CompanyDtos.CompanyListItem(
                c.getId(),
                c.getName(),
                c.getPhone(),
                c.getAddressLine(),
                c.getServiceRegionLabel(),
                new HashSet<>(c.getSpecialties()),
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