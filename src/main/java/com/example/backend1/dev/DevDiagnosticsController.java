package com.example.backend1.dev;

import com.example.backend1.common.ApiResponse;
import com.example.backend1.company.service.KakaoLocalClient;
import com.example.backend1.company.service.NaverSearchClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ⚠ DEV 전용 진단 엔드포인트.
 *
 * 외부 API (카카오 로컬, 네이버 NCP Geocoding) 가 실제로 응답을 주는지
 * 브라우저/Swagger 에서 바로 확인하기 위한 용도.
 *
 * SecurityConfig 에서 "/api/dev/**" 를 permitAll 로 열어 두어 토큰 없이도 호출 가능.
 * 운영 배포 시 이 파일은 삭제하거나 ADMIN 전용 경로로 옮길 것.
 */
@Tag(name = "Dev - Diagnostics (DEV ONLY)")
@RestController
@RequestMapping("/api/dev")
public class DevDiagnosticsController {

    private final KakaoLocalClient kakaoLocalClient;
    private final NaverSearchClient naverSearchClient;

    public DevDiagnosticsController(KakaoLocalClient kakaoLocalClient,
                                    NaverSearchClient naverSearchClient) {
        this.kakaoLocalClient = kakaoLocalClient;
        this.naverSearchClient = naverSearchClient;
    }

    /**
     * 카카오 로컬 키워드 검색 원본 응답을 그대로 반환.
     *
     * 예) /api/dev/kakao-test?q=편의점&lat=37.4979&lng=127.0276&radius=10000
     *
     * - body.meta.total_count 가 0 이 아니면 카카오 자체는 정상 동작.
     * - 예외가 나면 ok=false 와 exception 메시지가 같이 내려오고,
     *   서버 콘솔에는 [KakaoLocalClient] HTTP ... body=... 원문이 찍힘.
     */
    @Operation(summary = "Kakao Local API 진단 (DEV ONLY)")
    @GetMapping("/kakao-test")
    public ApiResponse<Map<String, Object>> kakaoTest(
            @RequestParam(defaultValue = "편의점") String q,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(defaultValue = "10000") int radius
    ) {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        Map<String, Object> requested = new LinkedHashMap<>();
        requested.put("q", q);
        requested.put("lat", lat);
        requested.put("lng", lng);
        requested.put("radius", radius);
        wrapper.put("requested", requested);

        try {
            Map<String, Object> body = kakaoLocalClient.searchKeyword(q, lat, lng, radius);
            wrapper.put("ok", true);
            // body.meta.total_count 를 요약해서 위로 끌어 올린다 — Swagger 에서 한눈에 보이게.
            if (body != null && body.get("meta") instanceof Map<?, ?> meta) {
                wrapper.put("total_count", meta.get("total_count"));
                wrapper.put("pageable_count", meta.get("pageable_count"));
            }
            // documents 는 그대로 붙여서 내림.
            if (body != null) {
                wrapper.put("documents_size",
                        (body.get("documents") instanceof java.util.List<?> l) ? l.size() : 0);
            }
            wrapper.put("body", body);
        } catch (Exception e) {
            wrapper.put("ok", false);
            wrapper.put("error", e.toString());
            wrapper.put("hint", "서버 콘솔에서 [KakaoLocalClient] HTTP ... body=... 원문 확인");
        }
        return ApiResponse.ok(wrapper);
    }

    /**
     * 네이버 NCP Geocoding 원본 응답 — 자동 지오코딩(관리자 등록 시 주소→좌표)이 잘 되는지 확인용.
     *
     * 예) /api/dev/geocode-test?address=서울특별시 강남구 테헤란로 427
     */
    @Operation(summary = "Naver NCP Geocoding 진단 (DEV ONLY)")
    @GetMapping("/geocode-test")
    public ApiResponse<Map<String, Object>> geocodeTest(@RequestParam String address) {
        Map<String, Object> wrapper = new LinkedHashMap<>();
        wrapper.put("address", address);
        try {
            Map<String, Object> body = naverSearchClient.getGeocode(address);
            wrapper.put("ok", true);
            if (body != null && body.get("addresses") instanceof java.util.List<?> list && !list.isEmpty()
                    && list.get(0) instanceof Map<?, ?> first) {
                Map<String, Object> picked = new HashMap<>();
                picked.put("x_longitude", first.get("x"));
                picked.put("y_latitude", first.get("y"));
                picked.put("roadAddress", first.get("roadAddress"));
                picked.put("jibunAddress", first.get("jibunAddress"));
                wrapper.put("first", picked);
            }
            wrapper.put("body", body);
        } catch (Exception e) {
            wrapper.put("ok", false);
            wrapper.put("error", e.toString());
        }
        return ApiResponse.ok(wrapper);
    }
}
