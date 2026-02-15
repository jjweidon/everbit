# 테스트 매트릭스 (Unit / Integration / E2E / 성능)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

원칙:
- FRD를 테스트로 환원한다.
- 주문/멱등/레이트리밋은 회귀 테스트로 고정한다.

---

## 1. 레이어 정의

### Backend
- Unit: JUnit5 + AssertJ
- Integration: SpringBootTest + Testcontainers(Postgres/Redis/Kafka)
- Contract/Stub: WireMock(Upbit REST), WS mock(가능하면)
- Performance: k6 또는 Gatling
- Security: secret scan + dependency scan(최소)

### Frontend
- Unit: Vitest
- Component: React Testing Library
- E2E: Playwright
- Push: Service Worker/구독 UX 일부는 mock 기반 검증

---

## 2. 기능별 매트릭스

표기:
- ✅ 필수
- ◻︎ 권장
- — 범위 밖

| 기능 | Unit | Integration | Contract(Upbit/Kakao) | E2E | 성능 | 비고 |
|---|---:|---:|---:|---:|---:|---|
| 카카오 로그인(OAuth2) | ✅ | ✅ | ◻︎ | ✅ | — | OWNER 고정 포함 |
| 1인 전용 계정 락 | ✅ | ✅ | — | ✅ | — | 다른 계정 403 |
| JWT 발급/검증/만료/rotation | ✅ | ✅ | — | ✅ | — | refresh_jti 포함 |
| 업비트 키 등록/검증/폐기 | ✅ | ✅ | ✅ | ✅ | ◻︎ | DB 암호화 포함 |
| 주문 금액 최소/최대 | ✅ | ✅ | — | ✅ | — | UI 포함 |
| 손절/익절/트레일링(리스크) | ✅ | ✅ | — | ✅ | ◻︎ | edge case |
| Kill Switch(계정/전략) | ✅ | ✅ | — | ✅ | — | executor 강제 |
| Signal 멱등(유니크) | ✅ | ✅ | — | — | — | UNIQUE 제약 |
| OrderIntent 멱등(유니크) | ✅ | ✅ | — | — | — | UNIQUE 제약 |
| OrderAttempt 재실행 안전(idempotent) | ✅ | ✅ | — | — | — | 중복 소비 no-op |
| Upbit 주문 생성(성공/4xx) | ◻︎ | ✅ | ✅ | ✅ | ◻︎ | WireMock |
| Upbit 429 처리(THROTTLED) | ✅ | ✅ | ✅ | — | ✅ | 백오프/재시도(새 Attempt) |
| Upbit 418 처리(BLOCKED) | ✅ | ✅ | ✅ | — | — | 차단 해제까지 중단 |
| timeout/5xx → UNKNOWN 수렴 | ✅ | ✅ | ✅ | — | — | reconcile 실패 시 SUSPENDED |
| Reconciliation 루프 | ✅ | ✅ | ✅ | — | ◻︎ | open orders/balance |
| 백테스트(멀티 마켓/TF) | ✅ | ✅ | — | ✅ | ✅ | 지표 산식 고정 |
| 대시보드(상태/주문/잔고/손익) | ◻︎ | ✅ | — | ✅ | ◻︎ | |
| 푸시 구독 등록/해지 | ◻︎ | ✅ | — | ✅ | — | Service Worker |
| 주문 접수 푸시(OrderAccepted) | ◻︎ | ✅ | ✅ | ◻︎ | — | eventId 중복 방지 |

---

## 3. 필수 E2E 시나리오(MVP)

### E2E-001: 최초 로그인 및 OWNER 고정
- Given: user 없음
- When: 카카오 로그인
- Then: user 생성 + JWT 발급
- And: 다른 카카오 계정 로그인은 403

### E2E-002: 업비트 키 등록 → 검증 → 암호화 저장
- When: 키 등록
- Then: 검증 성공 시에만 저장
- And: DB에는 암호문만 존재(평문 없음)

### E2E-003: 트레이딩 실행/중단(Kill Switch)
- Given: 키 등록 + 마켓 enabled
- When: 전략 실행 ON
- Then: 주문 intent/attempt 생성 → 주문 생성(Stub)
- When: Kill Switch OFF
- Then: 신규 주문 생성/발행 금지

### E2E-004: 429/UNKNOWN/418 안전 동작(회귀)
- 429: THROTTLED 후 새 Attempt로 재시도
- timeout/5xx: UNKNOWN 수렴 후 확정 실패 시 SUSPENDED
- 418: 차단 해제까지 호출 중단 + 자동매매 중단

### E2E-005: 푸시 알림 구독 + 주문 접수 알림
- When: 푸시 사용 ON → 구독 등록
- And: 주문이 접수(OrderAccepted)
- Then: 푸시 알림 수신(또는 mock에서 전송 호출 검증)

---

## 4. 성능 스모크(초기)

- 백테스트:
  - 작은 데이터(예: 2 markets × 2 TF × 30일)로 CI 스모크
  - 큰 데이터는 로컬/야간으로 분리
- 주문 파이프라인:
  - 429 상황에서도 프로세스가 다운되지 않고 큐 적체로 degrade 되는지 확인
