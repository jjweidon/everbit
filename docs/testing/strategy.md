# 테스트 전략

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- AI 생성 코드가 “그럴듯하게 돌아가 보이는” 수준을 넘어서, 실거래에서 안전하도록 만든다.
- 주문/멱등/레이트리밋은 **회귀 테스트로 고정**한다.

---

## 1. 기본 원칙

1) **FRD → 테스트 케이스**로 추적 가능해야 한다.
- 각 FR-ID는 최소 1개 이상의 테스트 케이스로 연결한다.

2) **외부 API는 스텁/컨트랙트 기반**
- 개발/CI에서는 실 Upbit/Kakao 호출을 최소화한다.
- 운영 검증 단계에서만 제한적으로 실 호출을 수행한다.

3) **실거래 사고 유발 영역은 P0 테스트로 승격**
- 중복 주문
- 429/418 대응
- timeout/5xx → UNKNOWN 수렴
- Kill Switch 강제

---

## 2. 테스트 레이어

### 2.1 Backend
- Unit: 도메인 규칙(전략 계산, 리스크 게이트, 상태머신)
- Integration(Testcontainers): Postgres/Redis/Kafka + Outbox 흐름
- Contract(Stub): WireMock(Upbit REST), (가능하면) WS mock
- E2E: API 레벨 + 프론트 Playwright
- Security: secret scanning, dependency scanning(최소)

### 2.2 Frontend
- Unit: 상태/유틸/훅
- Component: React Testing Library
- E2E: Playwright(로그인 → 키 등록 → 실행/중단 → 대시보드)
- Push: 권한/구독/수신 흐름을 컴포넌트 테스트 + e2e 일부로 검증

---

## 3. CI 게이트(머지 조건)

P0(필수):
- Unit 전체 통과
- Integration(Testcontainers) 전체 통과
- 주문 파이프라인 P0 시나리오(429/418/UNKNOWN/멱등) 통과
- secret scan 통과(키/토큰 패턴)
- lint/format 통과

P1(권장):
- E2E smoke(핵심 화면 1회)
- 성능 스모크(백테스트 작은 데이터)

---

## 4. 스텁 정책(Upbit/Kakao)

- Upbit REST:
  - CreateOrder: 성공/429/418/timeout/5xx/4xx 케이스를 WireMock으로 고정
  - GetOrder: identifier/uuid 조회 케이스 고정
- Kakao OAuth:
  - 로컬은 테스트 토큰/스텁로 단순화 가능
  - 운영 전에는 실제 OAuth 플로우 스모크 필수

---

## 5. Push(Web Push) 테스트 원칙

- 서버:
  - 구독 등록/해지 API 통합 테스트
  - 전송 실패(410) 시 구독 비활성화 테스트
- 클라이언트:
  - 권한 상태별 UI 분기 테스트
  - 구독 생성 실패/거부 케이스 테스트

---

## 6. 출력물
- `docs/testing/test-matrix.md`가 케이스 목록의 최종 기준이다.
- 성능/지표 산식은 `docs/testing/performance-plan.md`가 최종 기준이다.
