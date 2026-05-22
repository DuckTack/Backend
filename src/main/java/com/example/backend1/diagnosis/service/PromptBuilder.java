package com.example.backend1.diagnosis.service;

import com.example.backend1.ai.yolo.YoloResponse;
import com.example.backend1.user.domain.User;

/**
 * OpenAI Chat Completion 호출 시 system / user 프롬프트를 만든다.
 *
 * <p>프롬프트 엔지니어링은 정답이 없으니 AI 팀과 함께 반복 튜닝할 영역.
 * <p>현재는 "JSON 강제 출력 + 한국어 + 보수적 안내" 3원칙으로 구성.
 */
final class PromptBuilder {

    private PromptBuilder() {}

    static String systemPrompt() {
        return """
                너는 한국의 집수리 전문 AI 어시스턴트야.
                YOLO 객체 탐지 모델이 감지한 하자 정보와 사용자 컨텍스트를 받아서,
                사용자에게 적합한 DIY 가이드와 필요한 자재를 추천해.

                반드시 아래 JSON 스키마로만 응답해. 다른 텍스트는 절대 포함하지 마.
                {
                  "guide": {
                    "title": "<진단 제목, 한국어>",
                    "summary": "<2~3문장의 핵심 요약, 한국어>",
                    "difficulty": "easy" | "medium" | "hard",
                    "estimated_time_min": <정수 분>,
                    "steps": [
                      {"order": 1, "title": "<단계명>", "description": "<자세한 설명>", "warning": null}
                    ],
                    "warnings": ["<주의사항>"],
                    "next_action": "DIY_OK" | "RECALL_IN_24H" | "CALL_PRO"
                  },
                  "products": [
                    {
                      "name": "<자재 이름>",
                      "category": "<카테고리>",
                      "search_keyword": "<쿠팡 검색용 한국어 키워드>",
                      "reason": "<왜 필요한지>",
                      "quantity": <정수>,
                      "estimated_price": <정수 원>
                    }
                  ]
                }

                작성 원칙:
                - 위험도가 HIGH 이거나 누수/전기/가스 관련이면 next_action 을 CALL_PRO 로 권장한다.
                - 사용자가 한국에서 쿠팡으로 바로 살 수 있는 자재로 추천한다.
                - steps 는 3~6단계, 한국어로 명확하고 안전 지향적으로 작성한다.
                - 추측하지 않는다. 모르면 "전문가 호출" 로 안내한다.
                """;
    }

    static String userPrompt(YoloResponse yolo, RiskCalculator.RiskResult risk, User user) {
        return userPrompt(yolo, risk, user, false);
    }

    static String userPrompt(YoloResponse yolo, RiskCalculator.RiskResult risk, User user, boolean preferDiy) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== AI 객체 탐지 결과 ===\n");
        sb.append("주요 결함: ").append(risk.mainDefect()).append("\n");
        sb.append("위험도 등급: ").append(risk.level()).append("\n");
        sb.append(String.format("위험도 점수: %.2f (0~1 스케일)%n", risk.score()));
        sb.append("감지된 결함 개수: ").append(risk.detectionCount()).append("\n\n");

        sb.append("감지된 모든 결함:\n");
        if (yolo != null && yolo.detections() != null) {
            for (YoloResponse.Detection d : yolo.detections()) {
                sb.append("- class=").append(d.toDefectClass().code());
                sb.append(", confidence=").append(d.confidence());
                sb.append(", area_ratio=").append(d.areaRatio() == null ? "N/A" : d.areaRatio());
                sb.append("\n");
            }
        }

        sb.append("\n=== 사용자 컨텍스트 ===\n");
        sb.append("거주 형태: ").append(user.getResidenceType()).append("\n");
        sb.append("임대 형태: ").append(user.getRentType()).append("\n");

        if (preferDiy) {
            sb.append("\n⚠️ 사용자가 위험도와 관계없이 DIY 방법을 직접 원하고 있어.");
            sb.append("\n전문가 호출을 권장하되, 사용자가 직접 할 수 있는 DIY 단계도 함께 제공해.");
            sb.append("\nnext_action 은 DIY_OK 또는 RECALL_IN_24H 로 설정해. CALL_PRO 는 사용하지 마.");
        }

        sb.append("\n위 정보를 바탕으로 적합한 DIY 가이드와 필요 자재를 JSON 으로만 응답해.");
        return sb.toString();
    }
}
