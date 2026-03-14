# API 서버 구현 가이드 — 문서 기반 단계별 진행

Status: **Draft**  
Owner: everbit  
Last updated: 2026-03-14 (Asia/Seoul)

목적:
- **문서(SoT)**를 기준으로 클라이언트가 필요로 하는 API를 파악하고, 서버에서 단계별로 구현할 때 따를 가이드를 제공한다.
- AGENTS.md §1(문서 우선), §9(백엔드 레이어링/트랜잭션) 및 docs/api/contracts.md를 준수한다.

관련 문서:
- **API 계약 SoT**: `docs/api/contracts.md`
- **기능 요구사항**: `docs/requirements/functional.md`
- **인증 플로우**: `docs/integrations/kakao-oauth-auth-flow.md`
- **프론트 타입**: `client/src/types/api-contracts.ts`, `client/src/lib/api/endpoints.ts`

---

## 1. 클라이언트 필요 API 파악

### 1.1 계약서 기준 전체 엔드포인트 (contracts.md §1)

| Method | Path | 설명 | FR/ADR |
|--------|------|------|--------|
| **인증** | | | |
| GET | /api/v2/auth/start | OAuth 시작 → Kakao 302 | FR-AUTH-001 |
| GET | /api/v2/auth/callback | OAuth 콜백, 토큰 발급 | FR-AUTH-001, ADR-0007 |
| POST | /api/v2/auth/refresh | Access Token 재발급 | ADR-0007 |
| POST | /api/v2/auth/logout | 로그아웃(쿠키/Redis 폐기) | FR-AUTH-001 |
| **Upbit 키** | | | |
| GET | /api/v2/upbit/key/status | 키 등록·검증 결과 | FR-UPBIT-KEY-001 |
| POST | /api/v2/upbit/key | 키 등록+검증 | FR-UPBIT-KEY-001 |
| DELETE | /api/v2/upbit/key | 키 폐기 | FR-UPBIT-KEY-003 |
| **대시보드/마켓/전략** | | | |
| GET | /api/v2/dashboard/summary | 대시보드 요약 | - |
| GET | /api/v2/markets | 마켓 목록 | FR-TRADE-001 |
| PUT | /api/v2/markets/{market} | 마켓 설정 수정 | FR-TRADE-001 |
| POST | /api/v2/markets/{market}/unsuspend | SUSPENDED 수동 해제 | order-pipeline |
| GET | /api/v2/strategy/config | 전략 설정 조회 | - |
| PUT | /api/v2/strategy/config | 전략 설정 수정 | - |
| **주문** | | | |
| GET | /api/v2/orders | 주문 목록(페이징) | order-pipeline |
| GET | /api/v2/orders/{upbitUuid} | 주문 상세 | - |
| POST | /api/v2/reconcile | reconcile 트리거 | order-pipeline §7 |
| **푸시** | | | |
| POST | /api/v2/push/subscriptions | 구독 등록/갱신 | ADR-0008 |
| GET | /api/v2/push/subscriptions | 구독 목록 | ADR-0008 |
| DELETE | /api/v2/push/subscriptions/{id} | 구독 해지 | ADR-0008 |
| POST | /api/v2/push/test | 테스트 푸시 | ADR-0008 |
| **백테스트** | | | |
| GET | /api/v2/backtests | 백테스트 job 목록 | P2 |
| POST | /api/v2/backtests | 백테스트 실행 | P2 |
| GET | /api/v2/backtests/{jobPublicId} | 백테스트 상세 | P2 |

### 1.2 클라이언트 실제 사용처 매핑

| 클라이언트 위치 | 호출 API | 비고 |
|-----------------|----------|------|
| AuthContext | POST /auth/refresh | 페이지 로드 시 토큰 갱신 |
| login/page | GET OAuth authorization (Spring: /oauth2/authorization/kakao) | 로그인 시작 |
| auth/complete/page | GET /upbit/key/status | 로그인 후 키 유무 확인 |
| settings/upbit-key/page | GET /upbit/key/status, POST /upbit/key, DELETE /upbit/key | 키 설정 전부 |
| dashboard/page | GET /dashboard/summary, GET /orders, GET /markets, GET /upbit/key/status | 대시 진입 |
| markets/page | GET /markets, GET /dashboard/summary | 마켓 목록 + 리스크 배너 |
| orders/page | GET /orders, GET /dashboard/summary | 주문 목록 + 리스크 배너 |

즉, **현재 클라이언트가 실제로 사용하는 API**는:
- 인증: **auth/refresh** (OAuth start/callback은 Spring Security 기본 경로 사용 시 `/api/v2/oauth2/authorization/kakao`, `/api/v2/login/oauth2/code/kakao`)
- Upbit 키: **GET/POST/DELETE /upbit/key** (전부)
- 대시보드/마켓/주문: **GET /dashboard/summary**, **GET /markets**, **GET /orders**

추가로 구현하면 클라이언트에서 바로 활용 가능한 것:
- **POST /auth/logout** (설정 등에서 로그아웃 시)
- **PUT /markets/{market}**, **POST /markets/{market}/unsuspend** (마켓 설정/해제 UI)
- **GET /orders/{upbitUuid}**, **POST /reconcile** (주문 상세/재확인)
- **GET/PUT /strategy/config** (전략 설정 UI)
- 푸시/백테스트는 해당 UI 구현 시점에 맞춰 구현

---

## 2. 서버 구현 상태 — 인증·Upbit 키는 완료, 나머지 단계별 구현 대상

### 2.1 구현 완료로 간주하는 영역 (본 가이드에서는 구현 단계에서 제외)

| 구분 | 엔드포인트 | 비고 |
|------|------------|------|
| **인증** | POST /api/v2/auth/refresh | AuthController. OAuth start/callback은 Spring Security 경로 사용. |
| **인증** | (선택) POST /api/v2/auth/logout | 필요 시 별도 보완. |
| **Upbit 키** | GET /api/v2/upbit/key/status | UpbitKeyController |
| **Upbit 키** | POST /api/v2/upbit/key | 등록+검증 |
| **Upbit 키** | DELETE /api/v2/upbit/key | 키 폐기 |

이후 단계는 **위를 전제**로, **그 외 API만** 순서대로 구현한다.

### 2.2 단계별 구현 대상 (미구현 → 구현할 항목)

| 순서 | 구분 | 엔드포인트 | 우선순위 |
|------|------|------------|----------|
| **1** | 대시보드 | GET /api/v2/dashboard/summary | P0 (계약 검증·stub→실데이터) |
| **2** | 마켓 | GET /markets, PUT /markets/{market}, POST /markets/{market}/unsuspend | P0 |
| **3** | 주문 | GET /orders, GET /orders/{upbitUuid}, POST /reconcile | P0 |
| **4** | 전략 | GET/PUT /api/v2/strategy/config | P1 |
| **5** | 푸시 | POST/GET/DELETE /push/subscriptions, POST /push/test | P1 |
| **6** | 백테스트 | GET/POST/GET{id} /api/v2/backtests | P2 |

---

## 3. 단계별 구현 가이드 (인증·Upbit 키 제외)

인증·Upbit 키는 §2.1에 따라 **구현 완료로 간주**하며, 이 절에서는 **그 외 API만** 단계 1~6으로 구현한다.

### 원칙
1. **문서 고정 우선**: 계약(contracts.md) 및 FR/ADR에 DTO·에러코드·Reason Code가 명시되어 있는지 확인하고, 없으면 문서 보완 후 구현.
2. **한 번에 하나의 API 그룹**: 대시보드 → 마켓 → 주문 → 전략 → 푸시 → 백테스트 순으로 의존성을 고려해 진행.
3. **레이어링 준수**: Controller(DTO/검증/인가) → Service(비즈니스) → Adapter(Upbit 등 외부) → Repository(persistence). DB 트랜잭션 안에서 외부 API 호출 금지.
4. **에러/Reason Code**: contracts.md §9, §12와 NFR의 Reason Code 표준을 따르고, 동일 코드를 클라이언트 타입과 맞춘다.

---

### 단계 1: 대시보드 API (GET /dashboard/summary)

- **목표**: GET /dashboard/summary가 계약 §2와 일치하고, 가능한 경우 실제 리스크/자산 데이터와 연동.
- **문서**: `docs/api/contracts.md` §2, `client/src/types/api-contracts.ts` (DashboardSummary).
- **작업**:
  1. 응답 DTO를 계약과 동일하게: accountEnabled, strategyKey, strategyEnabled, wsStatus, lastReconcileAt, lastErrorAt, risk(throttled429Count24h, blocked418Until, unknownAttempts24h, suspendedMarkets), equity(equityKrw, realizedPnlKrw, unrealizedPnlKrw).
  2. 아직 읽기 모델/집계가 없다면 stub으로 고정값 반환하되, 필드명·타입은 계약과 동일하게 유지.
  3. 이후 order-pipeline·Upbit 연동이 들어가면 risk/equity를 실제 집계로 교체.

---

### 단계 2: 마켓 API (GET /markets, PUT, unsuspend)

- **목표**: GET /markets 구현 후, PUT /markets/{market}, POST /markets/{market}/unsuspend 순으로 구현.
- **문서**: `docs/api/contracts.md` §3, §9, §10, `docs/architecture/order-pipeline.md`, data-model.
- **작업**:
  1. **GET /api/v2/markets**  
     - 응답: `MarketStatusItem[]` (market, enabled, priority, positionStatus, tradeStatus, suspendReasonCode, lastSignalAt, cooldownUntil).  
     - client 타입 `MarketStatusItem`과 일치. tradeStatus: ACTIVE | SUSPENDED 등 계약 enum 유지.
  2. **PUT /api/v2/markets/{market}**  
     - Body: { enabled?, priority? }. 일부만 보내도 됨.  
     - 응답: 수정된 마켓 1개(GET /markets 항목과 동일 형식).
  3. **POST /api/v2/markets/{market}/unsuspend**  
     - SUSPENDED 마켓만 수동 해제. 응답: 해당 마켓 tradeStatus=ACTIVE 반영.
  4. 도메인/엔티티가 없으면 마켓 설정용 테이블·레포지토리 설계 시 data-model·ADR 정합성 확인.

---

### 단계 3: 주문 API (GET /orders, GET /orders/{upbitUuid}, POST /reconcile)

- **목표**: 주문 목록·상세·reconcile 트리거 구현. order-pipeline(멱등, UNKNOWN, 429/418) 정책 준수.
- **문서**: `docs/api/contracts.md` §6, §7, §8, `docs/architecture/order-pipeline.md`, `docs/architecture/data-model.md`.
- **작업**:
  1. **GET /api/v2/orders**  
     - Query: limit, cursor, market, attemptStatus, onlyAcked.  
     - 응답: items (OrderListItem[]), nextCursor.  
     - OrderListItem: intentPublicId, createdAt, market, side, intentType, requestedKrw/requestedVolume, reasonCode, latestAttempt(attemptPublicId, attemptNo, status, upbitUuid, nextRetryAt, errorCode, errorMessage).  
     - status는 PREPARED|SENT|ACKED|REJECTED|THROTTLED|UNKNOWN|SUSPENDED (contracts §6).
  2. **GET /api/v2/orders/{upbitUuid}**  
     - 404 시 code=ORDER_NOT_FOUND, reasonCode=UPBIT_UUID_UNKNOWN (contracts §7).
  3. **POST /api/v2/reconcile**  
     - Body optional: { scope: "full" | "open_orders_only" }.  
     - 202 Accepted, jobId·status·message 반환. DB 트랜잭션 내에서 Upbit 호출하지 않도록 주의(Outbox/비동기 처리).
  4. P0 테스트: 멱등, 429 처리, UNKNOWN 수렴, Kill Switch 차단 등은 AGENTS.md §10.1에 따라 추가.

---

### 단계 4: 전략 설정 API (GET/PUT /strategy/config)

- **목표**: 전략 설정 조회/수정. ADMIN-only 여부는 FR-AUTH-003에 따라 결정.
- **문서**: `docs/api/contracts.md` §4, §5, `docs/strategies/` (EVERBIT_MASTER_SPEC 및 개별 전략 스펙, 예: EXTREME_FLIP v1.1).
- **작업**:
  1. **GET /api/v2/strategy/config**  
     - 응답: strategyKey, configVersion, updatedAt, configJson (계약 §4).
  2. **PUT /api/v2/strategy/config**  
     - Body: { configJson }. 검증: minOrderKrw ≤ maxOrderKrw, maxOpenMarkets ≥ 1, 금액/비율/바 수 비음수.
  3. 저장소(DB 또는 설정 테이블) 설계 시 data-model과 일치시키기.

---

### 단계 5: 푸시 API (선택·UI 연동 시)

- **목표**: 구독 등록/목록/해지/테스트 푸시. ADR-0008, push-notifications §4 준수.
- **문서**: `docs/api/contracts.md` §1 푸시 행, `docs/architecture/push-notifications.md`.
- **작업**: 푸시 구독 엔드포인트 4개 요청/응답을 push-notifications 문서와 contracts에 맞춰 구현. 404/410 시 구독 비활성화 정책 준수.

---

### 단계 6: 백테스트 API (P2)

- **목표**: 백테스트 job 목록/실행/상세. P2이므로 MVP 이후 구현 가능.
- **문서**: `docs/api/contracts.md` §1 백테스트 행, client 타입 BacktestJobItem·BacktestDetail.

---

## 4. 구현 시 공통 체크리스트

- [ ] **문서**: 해당 API의 contracts.md 섹션과 FR/ADR이 구현 내용과 일치하는지 확인.
- [ ] **DTO**: 요청/응답 필드명 camelCase, ISO 8601 타임스탬프. client `api-contracts.ts`와 동기화.
- [ ] **인가**: 인증 필수(401), 역할/Everbit Key 조건(403) 반영. ADMIN-only 엔드포인트는 명시적 체크.
- [ ] **에러**: 표준 에러 본문(code, message, reasonCode, details). contracts §9, §11, §12 준수.
- [ ] **보안**: Upbit 키·JWT·시크릿 로그/응답 노출 금지. AGENTS.md §4.
- [ ] **트랜잭션**: DB 트랜잭션 내부에서 Upbit 등 외부 호출 금지. Outbox/비동기 시 문서 반영.
- [ ] **테스트**: P0 경로(주문 멱등, 429/418, UNKNOWN, Kill Switch 등)에 대한 테스트 추가 여부 확인.

---

## 5. 요약: 권장 진행 순서 (인증·Upbit 키 제외)

| 순서 | 그룹 | 작업 | 우선순위 |
|------|------|------|----------|
| 1 | 대시보드 | GET /dashboard/summary 계약 준수·stub→실데이터 전환 | P0 |
| 2 | 마켓 | GET /markets → PUT /markets/{market} → POST unsuspend | P0 |
| 3 | 주문 | GET /orders → GET /orders/{id} → POST /reconcile | P0 |
| 4 | 전략 | GET/PUT /strategy/config | P1 |
| 5 | 푸시 | 구독 등록/목록/해지/테스트 | P1 |
| 6 | 백테스트 | 목록/실행/상세 | P2 |

**전제**: 인증(refresh, OAuth)과 Upbit 키(GET/POST/DELETE /upbit/key)는 이미 구현 완료로 간주한다.  
클라이언트는 **dashboard/summary·markets·orders**를 사용하므로, 서버에서는 **단계 1(대시보드) 검증 후 단계 2·3(마켓·주문)** 구현을 우선하면 화면 연동이 가능해진다.
