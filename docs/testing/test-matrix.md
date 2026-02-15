# 테스트 매트릭스 (Unit / Integration / E2E / 부하)

## 1. 목적

Everbit v2는 AI 의존 개발을 전제로 한다. 따라서 “코드보다 테스트가 스펙”이 되도록 테스트 체계를 고정한다.

- 기능 요구사항(FRD)을 테스트 케이스로 환원
- 외부 API(Upbit/Kakao)는 **실거래/실 API 호출을 최소화**하고 Stub/Contract로 검증
- 주문/리스크/멱등성은 회귀 테스트로 고정

---

## 2. 테스트 레이어 정의

### 2.1 Backend (Spring Boot)
- Unit: JUnit5 + AssertJ
  - 도메인 규칙(리스크/지표 계산/identifier 생성) 중심
- Integration: SpringBootTest + Testcontainers
  - Postgres / Redis / Kafka 컨테이너
- Contract/Stub: WireMock (Upbit REST), 가짜 WebSocket 서버(가능하면)
- Component test: Kafka 메시지 흐름 + Outbox 동작
- Performance: Gatling 또는 k6 (API 부하, 백테스트 job 처리량)
- Security: dependency scanning, secret scanning, 기본 SAST

### 2.2 Frontend (Next.js)
- Unit: Vitest
- Component: React Testing Library
- E2E: Playwright (로그인 → 설정 → 실행 → 대시보드)

---

## 3. 기능별 테스트 매트릭스

표기:
- ✅ 필수
- ◻︎ 권장(시간 되면)
- — 범위 밖

| 기능 | Unit | Integration(Testcontainers) | Contract(Upbit/Kakao) | E2E(Playwright) | 부하/성능 | 비고 |
|---|---:|---:|---:|---:|---:|---|
| 카카오 로그인(OAuth2) | ✅ | ✅ | ◻︎ | ✅ | — | OAuth2 콜백/토큰 발급 |
| 1인 전용 계정 락 | ✅ | ✅ | — | ✅ | — | 다른 카카오 계정 차단 |
| JWT 발급/검증/만료 | ✅ | ✅ | — | ✅ | — | 쿠키/헤더 전략에 맞게 |
| 업비트 키 등록/검증/폐기 | ✅ | ✅ | ✅ | ✅ | ◻︎ | 키 암호화/검증 결과 |
| 업비트 키 DB 암호화 | ✅ | ✅ | — | — | — | 복호화 실패/키 회전 |
| 주문 금액 최소/최대(매수/매도) | ✅ | ✅ | — | ✅ | — | UI 버튼 포함 |
| 손절/익절/트레일링(리스크) | ✅ | ✅ | — | ✅ | ◻︎ | edge case 집중 |
| Kill Switch(계정/전략) | ✅ | ✅ | — | ✅ | — | executor에서 강제 |
| 주문 멱등성(신호/인텐트) | ✅ | ✅ | — | — | — | 유니크 제약/재기동 |
| 주문 생성 identifier 생성 규칙 | ✅ | ✅ | — | — | — | 길이/충돌/재사용 금지 |
| Upbit 주문 생성(성공/실패/timeout) | ◻︎ | ✅ | ✅ | ✅ | ◻︎ | WireMock으로 429/5xx |
| Upbit 429 레이트리밋 처리 | ✅ | ✅ | ✅ | — | ✅ | 백오프/큐 적체 |
| WebSocket myOrder/myAsset 수신 | ◻︎ | ◻︎ | ◻︎ | — | — | 가능하면 mock server |
| 정합성 Reconciliation 루프 | ✅ | ✅ | ✅ | — | ◻︎ | open orders/balance sync |
| 백테스트(멀티 마켓/타임프레임) | ✅ | ✅ | — | ✅ | ✅ | 지표(CAGR/MDD/승률/수익팩터) |
| 대시보드(상태/주문/잔고/손익) | ◻︎ | ✅ | — | ✅ | ◻︎ | read 모델 성능 |
| Prometheus 메트릭 노출 | — | ✅ | — | — | — | /actuator/prometheus |
| Grafana 대시보드 쿼리(스모크) | — | ◻︎ | — | — | — | 운영 점검용 |
| CI 파이프라인(Jenkins) | — | — | — | — | — | 빌드/테스트/배포 |

---

## 4. 필수 시나리오 기반 E2E (MVP)

### E2E-001: 최초 로그인 및 OWNER 고정
- Given: DB에 user 없음
- When: 카카오 로그인
- Then: user 생성 + JWT 발급
- And: 이후 다른 카카오 계정 로그인은 403

### E2E-002: 업비트 키 등록→검증→저장(암호화)
- When: 키 등록
- Then: 검증 성공 시에만 저장
- And: DB에는 암호문만 존재(평문 없음)

### E2E-003: 트레이딩 실행/중단(Kill Switch)
- Given: 키 등록 + 마켓 설정
- When: 전략 실행 ON
- Then: 주문 intent 생성 → 주문 생성(Stub)
- When: 헤더 Kill Switch OFF
- Then: 신규 주문 생성 금지

### E2E-004: 백테스트 실행 및 지표 확인
- When: 멀티 마켓/타임프레임 백테스트 실행
- Then: 결과 지표(CAGR/MDD/승률/수익팩터) 노출

---

## 5. 성능/부하 테스트 기준(초기)

- 백테스트:
  - N 마켓 × M 타임프레임 × T 기간에서
  - 1회 실행 시간이 임계값(예: 60초/5분)을 넘으면 원인 분석(쿼리/CPU/IO)
- 주문 파이프라인:
  - 429 대응 시 큐 적체가 발생해도 시스템이 다운되지 않아야 함
  - 주문 생성 그룹 초당 제한을 초과하지 않도록 스로틀링 검증

---

## 6. 테스트 데이터/모킹 정책

- 실 Upbit 호출은 “운영 검증” 단계에서만 제한적으로 수행
- 개발/CI는 WireMock 기반 스텁으로 고정
- WebSocket은 가능하면 로컬 mock server로 재현(끊김/재연결 포함)

---

## 7. Done 정의(테스트 관점)

- PR 머지 조건:
  - Unit + Integration 전체 통과
  - 주문/리스크/멱등성 관련 테스트는 커버리지 임계치(예: 80%) 강제
  - Secret scanning 통과(키/토큰 유출 방지)
