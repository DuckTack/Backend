package com.example.backend1.diagnosis.service;

import com.example.backend1.ai.AiAnalyzeRequest;
import com.example.backend1.ai.AiClient;
import com.example.backend1.ai.AiRawResponse;
import com.example.backend1.ai.DecisionService;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.diagnosis.repo.DiagnosisRepository;
import com.example.backend1.history.repo.HistoryRepository;
import com.example.backend1.report.service.ReportService;
import com.example.backend1.storage.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class AnalysisJobService {
    private static final Logger log = LoggerFactory.getLogger(AnalysisJobService.class);

    private final DiagnosisRepository diagnosisRepository;
    private final HistoryRepository historyRepository;
    private final FileService fileService;
    private final AiClient aiClient;
    private final DecisionService decisionService;
    private final ReportService reportService;

    public AnalysisJobService(
            DiagnosisRepository diagnosisRepository,
            HistoryRepository historyRepository,
            FileService fileService,
            AiClient aiClient,
            DecisionService decisionService,
            ReportService reportService
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.historyRepository = historyRepository;
        this.fileService = fileService;
        this.aiClient = aiClient;
        this.decisionService = decisionService;
        this.reportService = reportService;
    }

    @Async
    @Transactional
    public void run(String username, Long diagnosisId, Long historyId, List<String> imageKeys) {
        var diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));
        var history = historyRepository.findById(historyId)
                .orElseThrow(() -> new ApiException(ErrorCode.HISTORY_NOT_FOUND));

        try {
            List<String> urls = imageKeys.stream()
                    .map(k -> fileService.getOwnedPublicUrl(username, k))
                    .toList();

            AiRawResponse raw = aiClient.analyze(new AiAnalyzeRequest(urls));

            int risk = (raw != null && raw.riskScore() != null) ? raw.riskScore() : 0;
            IssueType issueType = toIssueType(raw);

            diagnosis.updateFromAiResult(AnalysisStatus.COMPLETED, risk, issueType);
            history.refreshFromDiagnosis();

            var decision = decisionService.evaluate(raw);

            try {
                reportService.generateAndAttach(
                        diagnosis.getId(),
                        decision.summary(),
                        decision.confidence(),
                        decision.finalAction(),
                        decision.reasons(),
                        decision.cautionNotes(),
                        decision.estimate()
                );
            } catch (Exception e) {
                log.warn("Report generation failed (diagnosisId={})", diagnosisId, e);
            }

        } catch (RestClientException e) {
            diagnosis.updateFromAiResult(AnalysisStatus.FAILED, diagnosis.getRiskScore(), diagnosis.getIssueType());
            history.refreshFromDiagnosis();
            log.warn("AI unavailable (diagnosisId={})", diagnosisId, e);
        } catch (Exception e) {
            diagnosis.updateFromAiResult(AnalysisStatus.FAILED, diagnosis.getRiskScore(), diagnosis.getIssueType());
            history.refreshFromDiagnosis();
            log.error("Analysis job failed (diagnosisId={})", diagnosisId, e);
        }
    }

    private IssueType toIssueType(AiRawResponse raw) {
        if (raw == null || raw.issueType() == null || raw.issueType().isBlank()) {
            return IssueType.ETC;
        }

        try {
            return IssueType.valueOf(raw.issueType().trim().toUpperCase());
        } catch (Exception e) {
            return IssueType.ETC;
        }
    }
}

