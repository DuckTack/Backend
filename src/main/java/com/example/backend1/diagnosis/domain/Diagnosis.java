package com.example.backend1.diagnosis.domain;

import com.example.backend1.user.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diagnosis", indexes = {
        @Index(name = "idx_diagnosis_user_created", columnList = "user_id, createdAt")
})
public class Diagnosis {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnalysisStatus status = AnalysisStatus.ANALYZING;

  // 🔥 수리 전 이미지 (진단 시 자동 저장)
  @ElementCollection
  @CollectionTable(
          name = "diagnosis_before_images",
          joinColumns = @JoinColumn(name = "diagnosis_id")
  )
  @Column(name = "image_key")
  private List<String> beforeImageKeys = new ArrayList<>();

  @Column(nullable = false)
  private Integer riskScore = 0;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IssueType issueType = IssueType.ETC;

  @Column(nullable = false)
  private OffsetDateTime createdAt = OffsetDateTime.now();

  @OneToOne(mappedBy = "diagnosis",
          cascade = CascadeType.ALL,
          orphanRemoval = true,
          fetch = FetchType.LAZY)
  private ReportMetadata report;

  protected Diagnosis() {}

  public Diagnosis(User user) {
    this.user = user;
  }

  // ================== getter ==================
  public Long getId() { return id; }
  public User getUser() { return user; }
  public AnalysisStatus getStatus() { return status; }
  public Integer getRiskScore() { return riskScore; }
  public IssueType getIssueType() { return issueType; }
  public OffsetDateTime getCreatedAt() { return createdAt; }
  public ReportMetadata getReport() { return report; }
  public List<String> getBeforeImageKeys() { return beforeImageKeys; }

  // ================== business ==================
  public void updateFromAiResult(AnalysisStatus status, Integer riskScore, IssueType issueType) {
    if (status != null) this.status = status;
    if (riskScore != null) this.riskScore = riskScore;
    if (issueType != null) this.issueType = issueType;
  }

  public void attachReport(ReportMetadata report) {
    this.report = report;
    if (report != null) report.attach(this);
  }

  // 🔥 before 이미지 설정
  public void setBeforeImageKeys(List<String> beforeImageKeys) {
    this.beforeImageKeys.clear();
    if (beforeImageKeys != null) {
      this.beforeImageKeys.addAll(beforeImageKeys);
    }
  }
}