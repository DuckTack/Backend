package com.example.backend1.admin.service;

import com.example.backend1.admin.dto.AdminCompanyDtos;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.company.domain.Company;
import com.example.backend1.company.repo.CompanyRepository;
import com.example.backend1.company.service.NaverSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Service
public class AdminCompanyService {

  private static final Logger log = LoggerFactory.getLogger(AdminCompanyService.class);

  private final CompanyRepository companyRepository;
  // ⭐ 주소 → 좌표 자동 변환에 사용 (NCP Maps Geocoding).
  //    관리자가 좌표를 비워두고 주소만 입력하면 서버가 대신 좌표를 채워 준다.
  private final NaverSearchClient naverSearchClient;

  public AdminCompanyService(CompanyRepository companyRepository,
                             NaverSearchClient naverSearchClient) {
    this.companyRepository = companyRepository;
    this.naverSearchClient = naverSearchClient;
  }

  /** 지오코딩 결과를 담는 내부 튜플 (둘 다 null 가능). */
  private record Coord(Double lat, Double lng) {}

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

    // ⭐ 좌표 자동 보정:
    //   - 요청 lat/lng 가 둘 다 있으면 그대로 사용.
    //   - 둘 중 하나라도 없고 addressLine(또는 기존 c.getAddressLine) 이 있으면 NCP Geocoding 호출.
    //   - 지오코딩 실패해도 업데이트 자체는 진행 (좌표만 null 로 유지).
    String effectiveAddress = req.addressLine() != null ? req.addressLine() : c.getAddressLine();
    Coord coord = resolveCoordinates(req.latitude(), req.longitude(), effectiveAddress);

    c.updateFrom(
            req.name(),
            req.businessRegistrationNumber(),
            req.representativeName(),
            req.phone(),
            req.email(),
            req.addressLine(),
            req.postalCode(),
            req.serviceRegionLabel(),
            coord.lat(),
            coord.lng(),
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
    // ⭐ create 경로에서도 동일하게 자동 지오코딩 적용.
    Coord coord = resolveCoordinates(req.latitude(), req.longitude(), req.addressLine());

    c.updateFrom(
            req.name(),
            req.businessRegistrationNumber(),
            req.representativeName(),
            req.phone(),
            req.email(),
            req.addressLine(),
            req.postalCode(),
            req.serviceRegionLabel(),
            coord.lat(),
            coord.lng(),
            req.specialties() != null ? new HashSet<>(req.specialties()) : new HashSet<>(),
            req.minEstimatedQuoteKrw(),
            req.maxEstimatedQuoteKrw(),
            req.capabilityNote(),
            req.active() != null ? req.active() : true,
            req.adminMemo()
    );
  }

  /**
   * 좌표 자동 해결.
   * 1) 요청 lat/lng 가 둘 다 있으면 그대로 반환.
   * 2) 둘 중 하나라도 비었고 addressLine 이 있으면 NCP Geocoding 으로 보정 시도.
   * 3) 지오코딩 실패 시 null 좌표 그대로 반환 (업체 저장 자체는 성공하도록).
   *
   * NCP Geocoding 응답 예시:
   * {
   *   "status":"OK",
   *   "meta":{...},
   *   "addresses":[{ "roadAddress":"...", "jibunAddress":"...", "x":"127.0276...", "y":"37.4979...", ... }],
   *   ...
   * }
   */
  private Coord resolveCoordinates(Double reqLat, Double reqLng, String addressLine) {
    if (reqLat != null && reqLng != null) {
      return new Coord(reqLat, reqLng);
    }
    if (addressLine == null || addressLine.isBlank()) {
      // 주소도 없고 좌표도 없음 — 보정 불가.
      return new Coord(reqLat, reqLng);
    }
    try {
      Map<String, Object> body = naverSearchClient.getGeocode(addressLine.trim());
      if (body == null) {
        log.warn("[AdminCompanyService] Geocoding body=null. address='{}'", addressLine);
        return new Coord(reqLat, reqLng);
      }
      Object addrObj = body.get("addresses");
      if (!(addrObj instanceof List<?> list) || list.isEmpty()) {
        log.warn("[AdminCompanyService] Geocoding no results. address='{}', status={}",
                addressLine, body.get("status"));
        return new Coord(reqLat, reqLng);
      }
      Object first = list.get(0);
      if (!(first instanceof Map<?, ?> m)) {
        return new Coord(reqLat, reqLng);
      }
      Object xObj = m.get("x"); // 경도 (longitude)
      Object yObj = m.get("y"); // 위도 (latitude)
      if (xObj == null || yObj == null) {
        return new Coord(reqLat, reqLng);
      }
      double lat = Double.parseDouble(String.valueOf(yObj));
      double lng = Double.parseDouble(String.valueOf(xObj));
      log.info("[AdminCompanyService] Geocoded '{}' -> lat={}, lng={}", addressLine, lat, lng);
      // 요청에 일부만 있었다면 요청값을 우선 존중 (방어적).
      Double finalLat = (reqLat != null) ? reqLat : lat;
      Double finalLng = (reqLng != null) ? reqLng : lng;
      return new Coord(finalLat, finalLng);
    } catch (Exception e) {
      log.warn("[AdminCompanyService] Geocoding failed, keep nulls. address='{}', cause={}",
              addressLine, e.toString());
      return new Coord(reqLat, reqLng);
    }
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
