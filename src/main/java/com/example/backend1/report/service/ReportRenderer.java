package com.example.backend1.report.service;

import com.example.backend1.ai.DecisionService;
import com.example.backend1.diagnosis.domain.Diagnosis;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportRenderer {

    public String toHtml(
            Diagnosis diagnosis,
            String summary,
            double confidence,
            String decision,
            List<String> reasons,
            List<String> cautionNotes,
            DecisionService.Estimate estimate
    ) {
        return """
        <html>
        <body style="font-family: Arial, sans-serif; padding: 24px; line-height: 1.6;">
          <h1>뚝딱 진단 리포트</h1>
          <hr/>
          <h2>기본 정보</h2>
          <p><b>진단 ID</b>: %d</p>
          <p><b>상태</b>: %s</p>
          <p><b>문제 유형</b>: %s</p>
          <p><b>위험도</b>: %d / 100</p>
          <p><b>신뢰도</b>: %.2f</p>
          <p><b>최종 판단</b>: %s</p>

          <h2>요약</h2>
          <p>%s</p>

          <h2>판단 사유</h2>
          %s

          <h2>주의 사항</h2>
          %s

          <h2>예상 견적</h2>
          <p><b>수준</b>: %s</p>
          <p><b>범위</b>: %d원 ~ %d원</p>
        </body>
        </html>
        """.formatted(
                diagnosis.getId(),
                diagnosis.getStatus().name(),
                diagnosis.getIssueType().name(),
                diagnosis.getRiskScore(),
                confidence,
                decision,
                summary == null ? "" : summary,
                toListHtml(reasons),
                toListHtml(cautionNotes),
                estimate != null ? estimate.level() : "UNKNOWN",
                estimate != null ? estimate.minWon() : 0,
                estimate != null ? estimate.maxWon() : 0
        );
    }

    private String toListHtml(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "<p>없음</p>";
        }

        StringBuilder sb = new StringBuilder("<ul>");
        for (String item : items) {
            sb.append("<li>").append(item).append("</li>");
        }
        sb.append("</ul>");
        return sb.toString();
    }
}