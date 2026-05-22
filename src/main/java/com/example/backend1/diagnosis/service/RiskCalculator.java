package com.example.backend1.diagnosis.service;

import com.example.backend1.ai.yolo.DefectClass;
import com.example.backend1.ai.yolo.YoloResponse;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * YOLO detection 결과를 위험도 점수/등급으로 환산하는 백엔드 자체 로직.
 *
 * <p>가중치/임계값은 도메인 전문가 인터뷰를 통해 튜닝하는 것을 권장한다.
 * <p>현재 값은 초기 MVP 용 휴리스틱:
 *
 * <pre>
 *   detection_score = severity_weight × confidence × area_boost
 *   total_score     = main_score + Σ secondary_score × 0.3   (1.0 으로 클램프)
 *   level           : ≥0.7 → HIGH, ≥0.4 → MEDIUM, &lt;0.4 → LOW (detections 없으면 NONE)
 * </pre>
 */
@Service
public class RiskCalculator {

    /** 결함 종류별 기본 위험 가중치 (0.0~1.0). */
    private static final Map<DefectClass, Double> SEVERITY_WEIGHT = Map.of(
            DefectClass.LEAK,       1.0,    // 누수 - 구조/전기 위험 → 최고
            DefectClass.GAS,        1.0,    // 가스 누출 → 최고
            DefectClass.ELECTRIC,   0.95,   // 전기 → 매우 위험
            DefectClass.CRACK,      0.8,    // 균열 - 구조 위험
            DefectClass.MOLD,       0.7,    // 곰팡이 - 건강
            DefectClass.DAMAGE,     0.6,    // 일반 파손
            DefectClass.STAIN,      0.3,    // 얼룩 - 미관
            DefectClass.PAINT_PEEL, 0.2,    // 도장 벗겨짐 - 미관
            DefectClass.OTHER,      0.5
    );

    /**
     * 단일 detection 의 위험 점수.
     */
    private double detectionScore(YoloResponse.Detection d) {
        DefectClass cls = d.toDefectClass();
        double base = SEVERITY_WEIGHT.getOrDefault(cls, 0.5);
        double conf = d.confidence() == null ? 0.5 : d.confidence();
        double area = d.areaRatio() == null ? 0.1 : d.areaRatio();

        // 면적이 클수록 위험 가중치 1.0~1.5 배 보정
        double areaBoost = 1.0 + Math.min(Math.max(area, 0.0) * 2.0, 0.5);

        return base * conf * areaBoost;
    }

    /**
     * YOLO 응답 → 종합 위험도 산출.
     */
    public RiskResult calculate(YoloResponse yolo) {
        if (yolo == null || yolo.detections() == null || yolo.detections().isEmpty()) {
            return new RiskResult(0.0, "NONE", DefectClass.OTHER, 0);
        }

        List<YoloResponse.Detection> detections = yolo.detections();

        // 가장 위험한 detection 을 메인 진단으로
        YoloResponse.Detection main = detections.stream()
                .max(Comparator.comparingDouble(this::detectionScore))
                .orElse(detections.get(0));

        double mainScore = detectionScore(main);

        // 부수 결함은 30% 만 가산
        double secondaryScore = detections.stream()
                .filter(d -> d != main)
                .mapToDouble(d -> detectionScore(d) * 0.3)
                .sum();

        double total = Math.min(1.0, mainScore + secondaryScore);

        String level;
        if (total >= 0.7)      level = "HIGH";
        else if (total >= 0.4) level = "MEDIUM";
        else                   level = "LOW";

        DefectClass mainDefect = main.toDefectClass();

        return new RiskResult(total, level, mainDefect, detections.size());
    }

    /**
     * 위험도 산출 결과.
     *
     * @param score      0.0 ~ 1.0
     * @param level      "NONE" | "LOW" | "MEDIUM" | "HIGH"
     * @param mainDefect 가장 위험한 결함 종류
     * @param detectionCount 감지된 결함 개수
     */
    public record RiskResult(
            double score,
            String level,
            DefectClass mainDefect,
            int detectionCount
    ) {
        /** 0~100 정수 스케일 (기존 Diagnosis.riskScore 와 호환). */
        public int score100() {
            return (int) Math.round(score * 100);
        }
    }
}
