package com.example.backend1.report.entity;

import com.example.backend1.diagnosis.domain.Diagnosis;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * 사용자가 수리 완료 후 입력하는 후입력 정보(드래프트).
 */
@Entity
@Table(name = "report_draft")
public class ReportDraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", unique = true, nullable = false)
    private Diagnosis diagnosis;

    /** 수리 방식 (DIY / PRO) */
    @Column(length = 200)
    private String repairMethod;

    /** 수리 완료일 */
    private LocalDate completionDate;

    /** 업체명 */
    @Column(length = 200)
    private String contractorName;

    /** 연락처 */
    @Column(length = 100)
    private String contractorContact;

    /** 작업 요약 */
    @Column(length = 2000)
    private String repairSummary;

    /** 총 비용(숫자형 요약 비용) */
    private Integer actualCostKrw;

    /** 메모 */
    @Column(length = 2000)
    private String notes;

    /** 총 비용(문자열 입력값) */
    @Column(length = 100)
    private String totalCost;

    /** 재료비 */
    @Column(length = 100)
    private String materialCost;

    /** 인건비 */
    @Column(length = 100)
    private String laborCost;

    /** DIY 사용 자재 */
    @Column(length = 1000)
    private String diyMaterialsUsed;

    /** DIY 자재비 */
    @Column(length = 100)
    private String diyMaterialCost;

    /** DIY 작업 메모 */
    @Column(length = 2000)
    private String diyWorkMemo;

    @Column(nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    protected ReportDraft() {}

    public ReportDraft(Diagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }

    public Long getId() { return id; }
    public Diagnosis getDiagnosis() { return diagnosis; }
    public String getRepairMethod() { return repairMethod; }
    public LocalDate getCompletionDate() { return completionDate; }
    public String getContractorName() { return contractorName; }
    public String getContractorContact() { return contractorContact; }
    public String getRepairSummary() { return repairSummary; }
    public String getNotes() { return notes; }public Integer getActualCostKrw() { return actualCostKrw; }
    public String getTotalCost() { return totalCost; }
    public String getMaterialCost() { return materialCost; }
    public String getLaborCost() { return laborCost; }
    public String getDiyMaterialsUsed() { return diyMaterialsUsed; }
    public String getDiyMaterialCost() { return diyMaterialCost; }
    public String getDiyWorkMemo() { return diyWorkMemo; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setTotalCost(String totalCost) {
        this.totalCost = totalCost;
    }

    public void update(
            String repairMethod,
            LocalDate completionDate,
            String contractorName,
            String contractorContact,
            String repairSummary,
            Integer actualCostKrw,
            String notes,
            String materialCost,
            String laborCost,
            String diyMaterialsUsed,
            String diyMaterialCost,
            String diyWorkMemo
    ) {
        if (repairMethod != null) this.repairMethod = repairMethod;
        if (completionDate != null) this.completionDate = completionDate;
        if (contractorName != null) this.contractorName = contractorName;
        if (contractorContact != null) this.contractorContact = contractorContact;
        if (repairSummary != null) this.repairSummary = repairSummary;
        if (actualCostKrw != null) this.actualCostKrw = actualCostKrw;
        if (notes != null) this.notes = notes;

        if (materialCost != null) this.materialCost = materialCost;
        if (laborCost != null) this.laborCost = laborCost;
        if (diyMaterialsUsed != null) this.diyMaterialsUsed = diyMaterialsUsed;
        if (diyMaterialCost != null) this.diyMaterialCost = diyMaterialCost;
        if (diyWorkMemo != null) this.diyWorkMemo = diyWorkMemo;

        this.updatedAt = OffsetDateTime.now();
    }
}