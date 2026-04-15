package com.example.backend1.report.service;

import com.example.backend1.common.ApiException;
import com.example.backend1.common.ErrorCode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;

@Service
public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);

    public byte[] htmlToPdf(String html) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {

            PdfRendererBuilder builder = new PdfRendererBuilder();

            // 폰트 먼저 등록
            File fontFile = new ClassPathResource("fonts/NanumGothic.ttf").getFile();
            builder.useFont(fontFile, "Nanum");

            // HTML 넣기
            builder.withHtmlContent(html, null);

            builder.toStream(os);
            builder.run();

            return os.toByteArray();

        } catch (Exception e) {
            log.error("PDF 렌더링 실패", e);
            throw new ApiException(ErrorCode.INTERNAL_ERROR, "PDF 생성에 실패했습니다.");
        }
    }
}