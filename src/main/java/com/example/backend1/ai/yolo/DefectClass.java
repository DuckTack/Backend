package com.example.backend1.ai.yolo;

import com.example.backend1.diagnosis.domain.IssueType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * YOLO 가 응답에 실어주는 결함 클래스를 백엔드 ENUM 으로 매핑.
 *
 * <p>현재 AI 팀 응답 스키마는 정수 {@code label} 을 사용한다 (학습된 클래스 인덱스).
 * <p>아래 {@link #LABEL_TO_DEFECT} 매핑은 <b>임시 추정값</b> 이라서
 * <b>AI 팀에서 실제 클래스 인덱스 ↔ 결함 매핑표를 받으면 반드시 수정해야 한다.</b>
 *
 * <p>예: AI 팀이 "0=crack, 1=leak, 2=mold" 라고 알려주면 매핑 테이블 그대로 반영.
 */
public enum DefectClass {

    LEAK       ("leak",       IssueType.LEAK),
    MOLD       ("mold",       IssueType.MOLD),
    CRACK      ("crack",      IssueType.CRACK),
    STAIN      ("stain",      IssueType.ETC),
    PAINT_PEEL ("paint_peel", IssueType.ETC),
    DAMAGE     ("damage",     IssueType.DAMAGE),
    ELECTRIC   ("electric",   IssueType.ELECTRIC),
    GAS        ("gas",        IssueType.GAS),
    OTHER      ("other",      IssueType.ETC);

    /**
     * ⚠ 임시 매핑 — AI 팀에서 받은 정확한 mapping 으로 교체할 것.
     *
     * <p>현재는 "알파벳 순서" 로 추정:
     * <pre>
     *   0 → CRACK
     *   1 → LEAK
     *   2 → MOLD
     * </pre>
     *
     * <p>AI 팀이 "0=leak, 1=mold, 2=crack" 같이 다른 순서로 학습시켰으면
     * 이 Map 만 수정하면 된다. (DB/도메인 ENUM 은 그대로 유지)
     */
    private static final Map<Integer, DefectClass> LABEL_TO_DEFECT = new HashMap<>();
    static {
        LABEL_TO_DEFECT.put(0, CRACK);       // crack  균열
        LABEL_TO_DEFECT.put(1, MOLD);        // mold   곰팡이
        LABEL_TO_DEFECT.put(2, LEAK);        // leak   누수
        LABEL_TO_DEFECT.put(3, PAINT_PEEL);  // peel   박리
        LABEL_TO_DEFECT.put(4, DAMAGE);      // corrosion 부식
        LABEL_TO_DEFECT.put(5, DAMAGE);      // bulge  팽창
    }

    private final String code;
    private final IssueType issueType;

    DefectClass(String code, IssueType issueType) {
        this.code = code;
        this.issueType = issueType;
    }

    public String code() { return code; }
    public IssueType issueType() { return issueType; }

    /** 정수 label → ENUM (매핑 테이블 미포함 / null 입력 시 OTHER). */
    public static DefectClass fromLabel(Integer label) {
        if (label == null) return OTHER;
        return LABEL_TO_DEFECT.getOrDefault(label, OTHER);
    }

    /** 문자열 코드 → ENUM (혹시 AI 팀이 추후 문자열로 바꾸면 그대로 사용 가능). */
    public static DefectClass fromCode(String raw) {
        if (raw == null || raw.isBlank()) return OTHER;
        String normalized = raw.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(d -> d.code.equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(OTHER);
    }
}
