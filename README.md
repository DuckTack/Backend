# Backend 1 (Core API & Auth)

## 포함 기능 (백엔드 1 역할)
- 회원가입/로그인, 아이디 중복 검사
- JWT 기반 인증/인가 (Bearer 토큰)
- 사용자 프로필 관리 (거주유형, 임대유형)
- 핵심 엔티티(User, Diagnosis, History, ReportMetadata) + 상태값(ANALYZING/COMPLETED/FAILED)
- 히스토리 목록/상세/필터(날짜/위험도/문제유형)/선택 삭제
- 공통 Response, 에러 코드, Validation, 예외 처리
- Swagger UI: `/swagger-ui.html`

## 실행
1) MySQL에 DB/계정 생성
- DB: `appdb`
- USER: `appuser` / PASS: `apppass` (application.yml과 일치시키거나 수정)

2) 실행
```bash
./gradlew bootRun
```

## 주요 API
- POST /api/auth/signup
- POST /api/auth/login
- GET  /api/auth/check-username?username=...
- GET  /api/users/me
- PUT  /api/users/me
- GET  /api/histories?from=&to=&riskMin=&riskMax=&status=COMPLETED&issueType=CRACK
- GET  /api/histories/{id}
- DELETE /api/histories  { "ids": [1,2,3] }

## 인증
- 로그인 응답 accessToken을 받아서 요청 헤더에 첨부
`Authorization: Bearer <token>`
