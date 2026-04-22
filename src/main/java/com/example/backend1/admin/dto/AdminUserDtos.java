package com.example.backend1.admin.dto;

import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.user.domain.RentType;
import com.example.backend1.user.domain.ResidenceType;
import com.example.backend1.user.domain.UserRole;

import java.time.OffsetDateTime;
import java.util.List;

public class AdminUserDtos {

    /** 목록용: 상세는 {@link UserDetail} + GET /api/admin/users/{id} */
    public record UserListItem(
            Long id,
            /** 회원 표시명으로 쓸 값(현재 스키마에는 별도 실명 필드 없음 → username) */
            String name,
            String address
    ) {}

    public record UserDetail(
            Long id,
            String username,
            UserRole role,
            String phoneNumber,
            String address,
            ResidenceType residenceType,
            RentType rentType,
            OffsetDateTime createdAt
    ) {}

    /** 관리자 - 특정 사용자의 진단 이력 항목 */
    public record UserHistoryItem(
            Long id,
            Long diagnosisId,
            AnalysisStatus status,
            Integer riskScore,
            IssueType issueType,
            OffsetDateTime createdAt
    ) {}
}