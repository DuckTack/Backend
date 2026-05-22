package com.example.backend1.ai;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * 외부 AI 인프라용 RestTemplate Bean 들.
 *
 * <p>기존 {@code AiClientConfig.aiRestTemplate} 은 내부 분석 서버용으로 그대로 두고,
 * 새로 추가된 GCP YOLO / OpenAI LLM 호출용은 별도 Bean 으로 분리한다 — 타임아웃/특성이 다르기 때문.
 *
 * <ul>
 *   <li>{@code yoloRestTemplate}  — multipart 이미지 업로드 + 모델 추론 시간 고려해 read 15s</li>
 *   <li>{@code openAiRestTemplate} — LLM 응답 시간 고려해 read 60s</li>
 * </ul>
 */
@Configuration
public class AiHttpClientConfig {

    @Bean(name = "yoloRestTemplate")
    public RestTemplate yoloRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(15))
                .build();
    }

    @Bean(name = "openAiRestTemplate")
    public RestTemplate openAiRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(60))
                .build();
    }
}
