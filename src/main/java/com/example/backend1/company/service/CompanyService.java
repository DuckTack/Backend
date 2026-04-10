package com.example.backend1.company.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.dto.CompanyDtos;
import com.example.backend1.company.repo.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

/**
 * 일반 사용자용 업체 서비스.
 * active = true 인 업체만 노출하며, 관리자 memo 등 내부 정보는 제외한다.
 */
@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    /** active 업체 목록 페이징 조회 */
    @Transactional(readOnly = true)
    public Page<CompanyDtos.CompanyListItem> listActive(Pageable pageable) {
        return companyRepository.findByActive(true, pageable).map(this::toListItem);
    }

    /** active 업체 단건 상세 조회 (숨김 업체 접근 시 NOT_FOUND) */
    @Transactional(readOnly = true)
    public CompanyDtos.CompanyDetail getActive(Long id) {
        Company c = companyRepository.findById(id)
                .filter(Company::isActive)
                .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
        return toDetail(c);
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
