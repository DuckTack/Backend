package com.example.backend1.report.service;

import com.example.backend1.ai.DecisionService;
import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.example.backend1.diagnosis.domain.Diagnosis;
import com.example.backend1.diagnosis.domain.ReportMetadata;
import com.example.backend1.diagnosis.repo.DiagnosisRepository;
import com.example.backend1.storage.FileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReportService {

    private final DiagnosisRepository diagnosisRepository;
    private final ReportRenderer reportRenderer;
    private final PdfService pdfService;
    private final FileService fileService;

    public ReportService(
            DiagnosisRepository diagnosisRepository,
            ReportRenderer reportRenderer,
            PdfService pdfService,
            FileService fileService
    ) {
        this.diagnosisRepository = diagnosisRepository;
        this.reportRenderer = reportRenderer;
        this.pdfService = pdfService;
        this.fileService = fileService;
    }

    @Transactional
    public ReportMetadata generateAndAttach(
            Long diagnosisId,
            String summary,
            double confidence,
            String decision,
            List<String> reasons,
            List<String> cautionNotes,
            DecisionService.Estimate estimate
    ) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        String html = reportRenderer.toHtml(
                diagnosis,
                summary,
                confidence,
                decision,
                reasons,
                cautionNotes,
                estimate
        );

        byte[] pdfBytes = pdfService.htmlToPdf(html);
        var saved = fileService.saveReportPdf(diagnosis.getUser().getUsername(), pdfBytes);

        ReportMetadata metadata = new ReportMetadata(
                saved.key(),
                saved.contentType(),
                saved.sizeBytes()
        );

        diagnosis.attachReport(metadata);
        return metadata;
    }

    @Transactional(readOnly = true)
    public byte[] downloadByDiagnosisId(String username, Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findByIdAndUserUsername(diagnosisId, username)
                .orElseThrow(() -> new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND));

        if (diagnosis.getReport() == null) {
            throw new ApiException(ErrorCode.DIAGNOSIS_NOT_FOUND);
        }

        return fileService.downloadOwned(username, diagnosis.getReport().getStorageKey()).bytes();
    }
}