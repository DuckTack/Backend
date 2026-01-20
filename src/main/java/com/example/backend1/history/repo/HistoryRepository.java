package com.example.backend1.history.repo;

import com.example.backend1.history.service.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository
        extends JpaRepository<HistoryEntity, Long>,
        JpaSpecificationExecutor<HistoryEntity> {

  // 기존
  boolean existsByIdAndUserId(Long id, Long userId);
  void deleteByIdInAndUserId(List<Long> ids, Long userId);

  // 추가 (백엔드1 요구사항 충족용)
  List<HistoryEntity> findByUserId(Long userId);

  List<HistoryEntity> findByUserIdAndIssueType(Long userId, String issueType);

  List<HistoryEntity> findByUserIdAndRiskScoreGreaterThanEqual(Long userId, int riskScore);

  List<HistoryEntity> findByUserIdAndCreatedAtBetween(
          Long userId,
          LocalDateTime start,
          LocalDateTime end
  );
}
