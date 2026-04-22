package com.example.backend1.admin.service;

import com.example.backend1.admin.dto.AdminUserDtos;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.history.repo.HistoryRepository;
import com.example.backend1.history.service.HistoryEntity;
import com.example.backend1.user.domain.User;
import com.example.backend1.user.repo.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

  private final UserRepository userRepository;
  private final HistoryRepository historyRepository;

  public AdminUserService(UserRepository userRepository, HistoryRepository historyRepository) {
    this.userRepository = userRepository;
    this.historyRepository = historyRepository;
  }

  @Transactional(readOnly = true)
  public Page<AdminUserDtos.UserListItem> listUsers(Pageable pageable) {
    return userRepository.findAll(pageable).map(this::toListItem);
  }

  @Transactional(readOnly = true)
  public AdminUserDtos.UserDetail getUser(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));
    return toDetail(user);
  }

  /** 특정 사용자의 진단 이력 전체 조회 (관리자 전용) */
  @Transactional(readOnly = true)
  public List<AdminUserDtos.UserHistoryItem> getUserHistories(Long userId) {
    // 사용자 존재 여부 먼저 확인
    userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

    List<HistoryEntity> histories = historyRepository.findByUserId(userId);

    return histories.stream()
            .map(h -> new AdminUserDtos.UserHistoryItem(
                    h.getId(),
                    h.getDiagnosis() != null ? h.getDiagnosis().getId() : null,
                    h.getStatus(),
                    h.getRiskScore(),
                    h.getIssueType(),
                    h.getCreatedAt()
            ))
            .collect(Collectors.toList());
  }

  private AdminUserDtos.UserListItem toListItem(User u) {
    return new AdminUserDtos.UserListItem(
            u.getId(),
            u.getUsername(),
            u.getAddress()
    );
  }

  private AdminUserDtos.UserDetail toDetail(User u) {
    return new AdminUserDtos.UserDetail(
            u.getId(),
            u.getUsername(),
            u.getRole(),
            u.getPhoneNumber(),
            u.getAddress(),
            u.getResidenceType(),
            u.getRentType(),
            u.getCreatedAt()
    );
  }
}
