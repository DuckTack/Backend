package com.example.backend1.company.domain;

import com.example.backend1.diagnosis.domain.IssueType;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * AI 예상견적·문제유형·지역 등으로 사용자와 매칭할 수 있도록 설계한 업체 엔티티.
 */
@Entity
@Table(name = "companies", indexes = {
        @Index(name = "idx_companies_active", columnList = "active")
})
public class Company {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 200)
  private String name;

  @Column(name = "business_registration_number", length = 20)
  private String businessRegistrationNumber;

  @Column(name = "representative_name", length = 100)
  private String representativeName;

  @Column(length = 30)
  private String phone;

  @Column(length = 255)
  private String email;

  @Column(name = "address_line", length = 500)
  private String addressLine;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  /** 서비스 가능 지역(시도/구 등 자유 텍스트 — 매칭·AI 참고용) */
  @Column(name = "service_region_label", length = 200)
  private String serviceRegionLabel;

  private Double latitude;
  private Double longitude;

  /** 처리 가능한 문제 유형(진단 IssueType과 정합) */
  @ElementCollection(fetch = FetchType.LAZY)
  @CollectionTable(name = "company_specialties", joinColumns = @JoinColumn(name = "company_id"))
  @Enumerated(EnumType.STRING)
  @Column(name = "issue_type", length = 32)
  private Set<IssueType> specialties = new HashSet<>();

  /** 예상 견적 하한(원, 선택) */
  @Column(name = "min_estimated_quote_krw")
  private Integer minEstimatedQuoteKrw;

  /** 예상 견적 상한(원, 선택) */
  @Column(name = "max_estimated_quote_krw")
  private Integer maxEstimatedQuoteKrw;

  /** 시공 능력·특이사항 등 AI/매칭 참고 텍스트 */
  @Column(name = "capability_note", length = 2000)
  private String capabilityNote;

  @Column(nullable = false)
  private boolean active = true;

  @Column(name = "admin_memo", length = 2000)
  private String adminMemo;

  @Column(nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @Column(nullable = false)
  private OffsetDateTime updatedAt = OffsetDateTime.now();

  protected Company() {}

  public Company(String name) {
    this.name = name;
  }

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getBusinessRegistrationNumber() { return businessRegistrationNumber; }
  public String getRepresentativeName() { return representativeName; }
  public String getPhone() { return phone; }
  public String getEmail() { return email; }
  public String getAddressLine() { return addressLine; }
  public String getPostalCode() { return postalCode; }
  public String getServiceRegionLabel() { return serviceRegionLabel; }
  public Double getLatitude() { return latitude; }
  public Double getLongitude() { return longitude; }
  public Set<IssueType> getSpecialties() { return specialties; }
  public Integer getMinEstimatedQuoteKrw() { return minEstimatedQuoteKrw; }
  public Integer getMaxEstimatedQuoteKrw() { return maxEstimatedQuoteKrw; }
  public String getCapabilityNote() { return capabilityNote; }
  public boolean isActive() { return active; }
  public String getAdminMemo() { return adminMemo; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public OffsetDateTime getUpdatedAt() { return updatedAt; }

  public void updateFrom(
          String name,
          String businessRegistrationNumber,
          String representativeName,
          String phone,
          String email,
          String addressLine,
          String postalCode,
          String serviceRegionLabel,
          Double latitude,
          Double longitude,
          Set<IssueType> specialties,
          Integer minEstimatedQuoteKrw,
          Integer maxEstimatedQuoteKrw,
          String capabilityNote,
          Boolean active,
          String adminMemo
  ) {
    if (name != null) this.name = name;
    if (businessRegistrationNumber != null) this.businessRegistrationNumber = businessRegistrationNumber;
    if (representativeName != null) this.representativeName = representativeName;
    if (phone != null) this.phone = phone;
    if (email != null) this.email = email;
    if (addressLine != null) this.addressLine = addressLine;
    if (postalCode != null) this.postalCode = postalCode;
    if (serviceRegionLabel != null) this.serviceRegionLabel = serviceRegionLabel;
    if (latitude != null) this.latitude = latitude;
    if (longitude != null) this.longitude = longitude;
    if (specialties != null) {
      this.specialties.clear();
      this.specialties.addAll(specialties);
    }
    if (minEstimatedQuoteKrw != null) this.minEstimatedQuoteKrw = minEstimatedQuoteKrw;
    if (maxEstimatedQuoteKrw != null) this.maxEstimatedQuoteKrw = maxEstimatedQuoteKrw;
    if (capabilityNote != null) this.capabilityNote = capabilityNote;
    if (active != null) this.active = active;
    if (adminMemo != null) this.adminMemo = adminMemo;
    this.updatedAt = OffsetDateTime.now();
  }
}
