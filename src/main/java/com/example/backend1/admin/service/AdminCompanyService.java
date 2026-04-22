package com.example.backend1.admin.service;

import com.example.backend1.admin.dto.AdminCompanyDtos;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.repo.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class AdminCompanyService {

  private final CompanyRepository companyRepository;

  public AdminCompanyService(CompanyRepository companyRepository) {
    this.companyRepository = companyRepository;
  }

  @Transactional
  public AdminCompanyDtos.CompanyResponse create(AdminCompanyDtos.CreateRequest req) {
    validateQuoteRange(req.minEstimatedQuoteKrw(), req.maxEstimatedQuoteKrw());
    Company c = new Company(req.name());
    applyCreate(c, req);
    return toResponse(companyRepository.save(c));
  }

  @Transactional(readOnly = true)
  public Page<AdminCompanyDtos.CompanyListItem> list(Boolean activeOnly, Pageable pageable) {
    Page<Company> page;
    if (Boolean.TRUE.equals(activeOnly)) {
      page = companyRepository.findByActive(true, pageable);
    } else {
      page = companyRepository.findAll(pageable);
    }
    return page.map(this::toListItem);
  }

  @Transactional(readOnly = true)
  public AdminCompanyDtos.CompanyResponse get(Long id) {
    return toResponse(companyRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND)));
  }

  @Transactional
  public AdminCompanyDtos.CompanyResponse update(Long id, AdminCompanyDtos.UpdateRequest req) {
    Company c = companyRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
    Integer newMin = req.minEstimatedQuoteKrw() != null ? req.minEstimatedQuoteKrw() : c.getMinEstimatedQuoteKrw();
    Integer newMax = req.maxEstimatedQuoteKrw() != null ? req.maxEstimatedQuoteKrw() : c.getMaxEstimatedQuoteKrw();
    validateQuoteRange(newMin, newMax);
    c.updateFrom(
            req.name(),
            req.businessRegistrationNumber(),
            req.representativeName(),
            req.phone(),
            req.email(),
            req.addressLine(),
            req.postalCode(),
            req.serviceRegionLabel(),
            req.latitude(),
            req.longitude(),
            req.specialties(),
            req.minEstimatedQuoteKrw(),
            req.maxEstimatedQuoteKrw(),
            req.capabilityNote(),
            req.active(),
            req.adminMemo()
    );
    return toResponse(companyRepository.save(c));
  }

  @Transactional
  public AdminCompanyDtos.CompanyResponse setActive(Long id, boolean active) {
    Company c = companyRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.COMPANY_NOT_FOUND));
    c.updateFrom(null, null, null, null, null, null, null, null, null, null, null, null, null, null, active, null);
    return toResponse(companyRepository.save(c));
  }

  private void applyCreate(Company c, AdminCompanyDtos.CreateRequest req) {
    c.updateFrom(
            req.name(),
            req.businessRegistrationNumber(),
            req.representativeName(),
            req.phone(),
            req.email(),
            req.addressLine(),
            req.postalCode(),
            req.serviceRegionLabel(),
            req.latitude(),
            req.longitude(),
            req.specialties() != null ? new HashSet<>(req.specialties()) : new HashSet<>(),
            req.minEstimatedQuoteKrw(),
            req.maxEstimatedQuoteKrw(),
            req.capabilityNote(),
            req.active() != null ? req.active() : true,
            req.adminMemo()
    );
  }

  private void validateQuoteRange(Integer min, Integer max) {
    if (min != null && max != null && min > max) {
      throw new ApiException(ErrorCode.INVALID_INPUT, "예상 견적 하한이 상한보다 클 수 없습니다.");
    }
  }

  private AdminCompanyDtos.CompanyListItem toListItem(Company c) {
    return new AdminCompanyDtos.CompanyListItem(c.getId(), c.getName(), formatListAddress(c), c.isActive());
  }

  private static String formatListAddress(Company c) {
    String pc = c.getPostalCode();
    String line = c.getAddressLine();
    boolean hasPc = pc != null && !pc.isBlank();
    boolean hasLine = line != null && !line.isBlank();
    if (hasPc && hasLine) {
      return "[" + pc + "] " + line;
    }
    if (hasLine) {
      return line;
    }
    if (hasPc) {
      return pc;
    }
    return null;
  }

  private AdminCompanyDtos.CompanyResponse toResponse(Company c) {
    return new AdminCompanyDtos.CompanyResponse(
            c.getId(),
            c.getName(),
            c.getBusinessRegistrationNumber(),
            c.getRepresentativeName(),
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
            c.getCapabilityNote(),
            c.isActive(),
            c.getAdminMemo(),
            c.getCreatedAt(),
            c.getUpdatedAt()
    );
  }
}
