package com.example.backend1.report.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.report.service.ReportService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/diagnosis/{diagnosisId}/download")
    public ResponseEntity<byte[]> download(
            Authentication authentication,
            @PathVariable Long diagnosisId
    ) {
        byte[] pdf = reportService.downloadByDiagnosisId(authentication.getName(), diagnosisId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.pdf\"")
                .body(pdf);
    }

    @PostMapping("/diagnosis/{diagnosisId}/generate")
    public ApiResponse<String> generate(
            Authentication authentication,
            @PathVariable Long diagnosisId
    ) {
        reportService.generateAndAttach(
                diagnosisId,
                "AI 분석 결과 기반 리포트가 생성되었습니다.",
                0.0,
                "DIY",
                java.util.List.of(),
                java.util.List.of(),
                new com.example.backend1.ai.DecisionService.Estimate("UNKNOWN", 0, 0)
        );

        return ApiResponse.ok("generated");
    }
}