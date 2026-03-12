package com.example.backend1.diagnosis.service;

import com.example.backend1.ai.AiAnalyzeRequest;
import com.example.backend1.ai.AiClient;
import com.example.backend1.ai.AiRawResponse;
import com.example.backend1.ai.DecisionService;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.diagnosis.domain.AnalysisStatus;
import com.example.backend1.diagnosis.domain.Diagnosis;
import com.example.backend1.diagnosis.domain.IssueType;
import com.example.backend1.diagnosis.repo.DiagnosisRepository;
import com.example.backend1.history.repo.HistoryRepository;
import com.example.backend1.history.service.HistoryEntity;
import com.example.backend1.report.service.ReportService;
import com.example.backend1.storage.FileService;
import com.example.backend1.user.repo.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisAnalysisService {

    private final DiagnosisRepository diagnosisRepository;
    private final HistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final AiClient aiClient;
    private final DecisionService decisionService;
    private final ReportService reportService;
    private final AnalysisJobService analysisJobService;

    public DiagnosisAnalysisService(
            DiagnosisRepository diagnosisRepository,
            HistoryRepository historyRepository,
            UserRepository userRepository,
            FileService fileService,
            AiClient aiClient,
            DecisionService decisionService,
            ReportService reportService,
            AnalysisJobService analysisJobService
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.fileService = fileService;
        this.aiClient = aiClient;
        this.decisionService = decisionService;
        this.reportService = reportService;
        this.analysisJobService = analysisJobService;
    }

    @Transactional
    public StartResult start(String username, List<String> imageKeys) {
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        Diagnosis diagnosis = new Diagnosis(user);
        diagnosisRepository.save(diagnosis);

        HistoryEntity history = new HistoryEntity(user, diagnosis);
        historyRepository.save(history);

        analysisJobService.run(username, diagnosis.getId(), history.getId(), imageKeys);

        return new StartResult(
                diagnosis.getId(),
                history.getId(),
                diagnosis.getStatus().name()
        );
    }

    public record StartResult(Long diagnosisId, Long historyId, String status) {}
}