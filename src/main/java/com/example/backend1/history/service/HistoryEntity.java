package com.example.backend1.history.service;

import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.Diagnosis;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.user.domain.User;
import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "history", indexes = {
    @Index(name = "idx_history_user_created", columnList = "user_id, createdAt")
})
public class HistoryEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  private Diagnosis diagnosis;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AnalysisStatus status;

  @Column(nullable = false)
  private Integer riskScore;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private IssueType issueType;

  @Column(nullable = false)
  private OffsetDateTime createdAt;

  protected HistoryEntity() {}

  public HistoryEntity(User user, Diagnosis diagnosis) {
    this.user = user;
    this.diagnosis = diagnosis;
    this.status = diagnosis.getStatus();
    this.riskScore = diagnosis.getRiskScore();
    this.issueType = diagnosis.getIssueType();
    this.createdAt = diagnosis.getCreatedAt();
  }

  public Long getId() { return id; }
  public User getUser() { return user; }
  public Diagnosis getDiagnosis() { return diagnosis; }
  public AnalysisStatus getStatus() { return status; }
  public Integer getRiskScore() { return riskScore; }
  public IssueType getIssueType() { return issueType; }
  public OffsetDateTime getCreatedAt() { return createdAt; }

  public void refreshFromDiagnosis() {
    this.status = diagnosis.getStatus();
    this.riskScore = diagnosis.getRiskScore();
    this.issueType = diagnosis.getIssueType();
  }
  public HistoryEntity(
          User user,
          Diagnosis diagnosis,
          AnalysisStatus status,
          Integer riskScore,
          IssueType issueType
  ) {
    this.user = user;
    this.diagnosis = diagnosis;
    this.status = status;
    this.riskScore = riskScore;
    this.issueType = issueType;
    this.createdAt = OffsetDateTime.now();
  }

}
