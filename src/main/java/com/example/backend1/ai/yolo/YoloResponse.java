package com.example.backend1.ai.yolo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * YOLO 서버 응답 DTO.
 *
 * <p>실제 계약 (AI 팀 ngrok endpoint):
 * <pre>
 * POST https://frostily-sworn-shifting.ngrok-free.dev/detect
 * Content-Type: multipart/form-data, key=image
 *
 * Response:
 * {
 *   "status": "success",
 *   "count": 2,
 *   "detections": [
 *     {
 *       "label": 0,           // ← 정수! 클래스 인덱스
 *       "confidence": 0.91,
 *       "bbox": [x1, y1, x2, y2]
 *     }
 *   ]
 * }
 * </pre>
 *
 * <p>응답에 추가 필드(status, count) 가 와도 무시되도록 ignoreUnknown=true.
 * <p>area_ratio 는 현재 응답에 없지만 추후 추가될 수도 있어 옵셔널 필드로 유지.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record YoloResponse(
        String status,
        Integer count,
        List<Detection> detections
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Detection(
            Integer label,                                          // ⭐ class index (정수)
            Double confidence,
            List<Integer> bbox,                                     // [x1, y1, x2, y2]
            @JsonProperty("area_ratio") Double areaRatio            // 옵션
    ) {
        /** 정수 label → 백엔드 ENUM 매핑. */
        public DefectClass toDefectClass() {
            return DefectClass.fromLabel(label);
        }
    }
}
