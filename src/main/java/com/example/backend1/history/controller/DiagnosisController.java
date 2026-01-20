package com.example.backend1.diagnosis.controller;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.diagnosis.service.DiagnosisService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/diagnoses")
@SecurityRequirement(name = "bearerAuth")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping("/mock")
    public ApiResponse<Long> mock(Authentication authentication) {
        Long diagnosisId = diagnosisService.createMockDiagnosis(authentication.getName());
        return ApiResponse.ok(diagnosisId);
    }
}
