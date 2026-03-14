# Everbit v2 API 계약 (Read-model)

Status: **Draft**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

문서 기준:
- UI/UX: `docs/ui/everbit_ui_impl_spec.md`
- 주문 파이프라인: `docs/architecture/order-pipeline.md`
- 데이터 모델: `docs/architecture/data-model.md`
- 프론트 타입 SoT: `client/src/types/api-contracts.ts`

본 문서는 **프론트-백 API 계약**을 고정한다. 구현 전 문서 우선 규칙에 따라 계약을 확정한 뒤 서버 구현을 진행한다.

---

## 0. 공통 규칙

### 0.1 인증
- 모든 API(health 제외)는 `Authorization: Bearer <access_token>` 필수
- 세션 전략: ADR-0007 (Access: 메모리, Refresh: HttpOnly 쿠키)

### 0.2 Base Path
- `/api/v2`

### 0.3 응답 형식
- 성공: `application/json`, HTTP 2xx
- 실패: `application/json`, 표준 에러 본문(§9 참조)

### 0.4 필드 명명
- JSON 필드: **camelCase** (프론트 타입과 일치)
- ISO 8601: `timestamptz`(UTC) 문자열

### 0.5 필수 리스크 필드
다음 필드는 **운영 안전성**을 위해 응답에 포함되어야 한다.
- `UNKNOWN` / `SUSPENDED` / `THROTTLED`: Attempt 상태값
- `nextRetryAt`: THROTTLED(429) 시 재시도 예정 시각
- `blocked418Until`: 418 차단 해제 예정 시각(전역)
- `reasonCode` / `errorCode` / `errorMessage`: 차단/실패 사유

---

## 1. 엔드포인트 목록

API 계약 SoT는 본 문서다. UI 명세(`docs/ui/everbit_ui_impl_spec.md`)의 "권장 API"는 아래 경로를 따른다.

| Method | Path | 설명 |
|--------|------|------|
| **인증** | | |
| GET | /api/v2/auth/start | OAuth 시작. state 생성 후 Kakao redirect URL로 302 |
| GET | /api/v2/auth/callback | OAuth 콜백. code 교환, 회원 매핑, 토큰 발급 |
| POST | /api/v2/auth/refresh | Refresh 쿠키로 Access Token 재발급 (ADR-0007) |
| POST | /api/v2/auth/logout | 로그아웃(Refresh 쿠키/Redis 폐기) |
| **Upbit 키** | | |
| GET | /api/v2/upbit/key/status | 키 등록 여부·마지막 검증 결과 |
| POST | /api/v2/upbit/key | 키 등록+검증 (FR-UPBIT-KEY-001) |
| DELETE | /api/v2/upbit/key | 키 폐기 (FR-UPBIT-KEY-003) |
| **대시보드/마켓/전략** | | |
| GET | /api/v2/dashboard/summary | 대시보드 요약(실행·리스크·자산) |
| GET | /api/v2/markets | 마켓 목록(enable/priority/position/runtime) |
| PUT | /api/v2/markets/{market} | 마켓 설정 수정(enabled, priority) |
| POST | /api/v2/markets/{market}/unsuspend | 마켓 SUSPENDED 수동 해제 |
| GET | /api/v2/strategy/config | 전략 설정 조회 |
| PUT | /api/v2/strategy/config | 전략 설정 수정 |
| **주문** | | |
| GET | /api/v2/orders | 주문 목록(페이징) |
| GET | /api/v2/orders/{upbitUuid} | 주문 상세 |
| POST | /api/v2/reconcile | reconcile 트리거(운영용, 옵션) |
| **푸시** | | |
| POST | /api/v2/push/subscriptions | 구독 등록/갱신 |
| GET | /api/v2/push/subscriptions | 구독 목록 |
| DELETE | /api/v2/push/subscriptions/{id} | 구독 해지 |
| POST | /api/v2/push/test | 테스트 푸시 발송 |
| **백테스트** | | |
| GET | /api/v2/backtests | 백테스트 job 목록 |
| POST | /api/v2/backtests | 백테스트 실행 |
| GET | /api/v2/backtests/{jobPublicId} | 백테스트 결과 상세 |

푸시 API 요청/응답 상세: `docs/architecture/push-notifications.md` §4.  
인증/세션 상세: ADR-0007, `docs/requirements/non-functional.md` §1.2.  
카카오 OAuth 로그인 플로우: `docs/integrations/kakao-oauth-auth-flow.md`.

---

## 2. GET /api/v2/dashboard/summary

대시보드 상단 실행·리스크·자산 요약.

### 요청
- Headers: `Authorization: Bearer <token>`

### 응답 200 OK

```json
{
  "accountEnabled": true,
  "strategyKey": "EXTREME_FLIP",
  "strategyEnabled": true,
  "wsStatus": "CONNECTED",
  "lastReconcileAt": "2026-03-06T02:30:00.000Z",
  "lastErrorAt": "2026-03-06T01:15:00.000Z",
  "risk": {
    "throttled429Count24h": 2,
    "blocked418Until": null,
    "unknownAttempts24h": 1,
    "suspendedMarkets": ["KRW-BTC"]
  },
  "equity": {
    "equityKrw": "12500000",
    "realizedPnlKrw": "450000",
    "unrealizedPnlKrw": "-32000"
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| accountEnabled | boolean | ✓ | 계정 Kill Switch |
| strategyKey | string | ✓ | 현재 표시 전략 키 (EXTREME_FLIP / STRUCTURE_LIFT / PRESSURE_SURGE 등). strategy_config 기준, 없으면 EXTREME_FLIP |
| strategyEnabled | boolean | ✓ | 해당 전략의 실행 여부 (KillSwitch enabled_strategies 기준) |
| wsStatus | enum | ✓ | CONNECTED / DEGRADED / DISCONNECTED |
| lastReconcileAt | string | | 마지막 reconcile 시각 |
| lastErrorAt | string | | 마지막 오류 시각 |
| risk.throttled429Count24h | number | ✓ | 24h 내 429(THROTTLED) 건수 |
| risk.blocked418Until | string \| null | | 418 차단 해제 예정 시각 |
| risk.unknownAttempts24h | number | ✓ | 24h 내 UNKNOWN 건수 |
| risk.suspendedMarkets | string[] | ✓ | SUSPENDED 마켓 목록 |
| equity.equityKrw | string | ✓ | 총 자산(KRW) |
| equity.realizedPnlKrw | string | ✓ | 실현 손익 |
| equity.unrealizedPnlKrw | string | ✓ | 미실현 손익 |

---

## 3. GET /api/v2/markets

마켓 설정 및 포지션/실행 상태.

### 요청
- Headers: `Authorization: Bearer <token>`

### 응답 200 OK

```json
[
  {
    "market": "KRW-BTC",
    "enabled": true,
    "priority": 1,
    "positionStatus": "OPEN",
    "tradeStatus": "SUSPENDED",
    "suspendReasonCode": "UPBIT_UUID_UNKNOWN",
    "lastSignalAt": "2026-03-06T02:00:00.000Z",
    "cooldownUntil": "2026-03-06T04:00:00.000Z"
  },
  {
    "market": "KRW-ETH",
    "enabled": true,
    "priority": 2,
    "positionStatus": "OPEN",
    "tradeStatus": "ACTIVE",
    "lastSignalAt": "2026-03-06T03:30:00.000Z"
  }
]
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| market | string | ✓ | KRW-BTC 등 |
| enabled | boolean | ✓ | 활성 여부 |
| priority | number | ✓ | 우선순위(동시 신호 시) |
| positionStatus | enum | ✓ | FLAT / OPEN |
| tradeStatus | enum | ✓ | ACTIVE / SUSPENDED |
| suspendReasonCode | string | | SUSPENDED 사유 코드 |
| lastSignalAt | string | | 마지막 시그널 시각 |
| cooldownUntil | string | | 쿨다운 만료 시각 |

---

## 4. GET /api/v2/strategy/config

전략 설정 조회.

### 요청
- Headers: `Authorization: Bearer <token>`

### 응답 200 OK

```json
{
  "strategyKey": "EXTREME_FLIP",
  "configVersion": 3,
  "updatedAt": "2026-03-05T10:00:00.000Z",
  "configJson": {
    "timeframes": ["15", "60"],
    "regime": { "minStrength": 0.6 },
    "entry": { "minSignalStrength": 0.7, "maxOpenMarkets": 3 },
    "exit": { "stopLossPct": 0.03, "takeProfitPct": 0.05 },
    "risk": { "minOrderKrw": 50000, "maxOrderKrw": 500000 },
    "execution": { "cooldownMinutes": 60 }
  }
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| strategyKey | string | ✓ | EXTREME_FLIP |
| configVersion | number | ✓ | 설정 버전 |
| updatedAt | string | ✓ | 마지막 수정 시각 |
| configJson | object | ✓ | 파라미터 본문 |

---

## 5. PUT /api/v2/strategy/config

전략 설정 수정.

### 요청
- Headers: `Authorization: Bearer <token>`
- Body: `application/json`

```json
{
  "configJson": {
    "timeframes": ["15", "60"],
    "regime": { "minStrength": 0.6 },
    "entry": { "minSignalStrength": 0.7, "maxOpenMarkets": 3 },
    "exit": { "stopLossPct": 0.03, "takeProfitPct": 0.05 },
    "risk": { "minOrderKrw": 50000, "maxOrderKrw": 500000 },
    "execution": { "cooldownMinutes": 60 }
  }
}
```

### 응답 200 OK
- Body: GET /api/v2/strategy/config와 동일(갱신된 설정 반환)

### 검증 규칙(프론트 최소)
- minOrderKrw <= maxOrderKrw
- maxOpenMarkets >= 1
- 금액/비율/바 수는 음수 금지

---

## 6. GET /api/v2/orders

주문 목록(Intent + 최신 Attempt join).

### 요청
- Headers: `Authorization: Bearer <token>`
- Query:
  - `limit` (optional): 기본 20, 최대 100
  - `cursor` (optional): 이전 응답의 `nextCursor`로 페이지네이션
  - `market` (optional): 마켓 필터
  - `attemptStatus` (optional): PREPARED|SENT|ACKED|REJECTED|THROTTLED|UNKNOWN|SUSPENDED
  - `onlyAcked` (optional): true 시 ACKED만

### 응답 200 OK

```json
{
  "items": [
    {
      "intentPublicId": "018e1234-5678-7000-8000-000000000001",
      "createdAt": "2026-03-06T03:45:00.000Z",
      "market": "KRW-BTC",
      "side": "BUY",
      "intentType": "ENTRY",
      "requestedKrw": "500000",
      "reasonCode": "SIGNAL_STRENGTH_HIGH",
      "latestAttempt": {
        "attemptPublicId": "018e1234-5678-7000-8000-000000000002",
        "attemptNo": 1,
        "status": "ACKED",
        "upbitUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
        "nextRetryAt": null,
        "errorCode": null,
        "errorMessage": null
      }
    },
    {
      "intentPublicId": "018e1234-5678-7000-8000-000000000003",
      "createdAt": "2026-03-06T03:30:00.000Z",
      "market": "KRW-ETH",
      "side": "SELL",
      "intentType": "EXIT_TP",
      "requestedVolume": "0.5",
      "reasonCode": "TP_HIT",
      "latestAttempt": {
        "attemptPublicId": "018e1234-5678-7000-8000-000000000004",
        "attemptNo": 1,
        "status": "THROTTLED",
        "nextRetryAt": "2026-03-06T03:35:00.000Z",
        "errorCode": "RATE_LIMIT_429",
        "errorMessage": "Upbit rate limit exceeded"
      }
    },
    {
      "intentPublicId": "018e1234-5678-7000-8000-000000000005",
      "createdAt": "2026-03-06T02:00:00.000Z",
      "market": "KRW-BTC",
      "side": "BUY",
      "intentType": "ENTRY",
      "requestedKrw": "300000",
      "latestAttempt": {
        "attemptPublicId": "018e1234-5678-7000-8000-000000000006",
        "attemptNo": 1,
        "status": "UNKNOWN",
        "errorCode": "TIMEOUT",
        "errorMessage": "Upbit request timeout"
      }
    }
  ],
  "nextCursor": "018e1234-5678-7000-8000-000000000005"
}
```

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| items | array | ✓ | OrderListItem[] |
| nextCursor | string \| null | | 다음 페이지 커서, 없으면 null |

**OrderListItem.latestAttempt** 필수 필드:
- `status`: PREPARED|SENT|ACKED|REJECTED|**THROTTLED**|**UNKNOWN**|**SUSPENDED**
- `nextRetryAt`: THROTTLED 시 재시도 예정 시각
- `errorCode` / `errorMessage`: 실패/차단 사유

---

## 7. GET /api/v2/orders/{upbitUuid}

주문 상세(UpbitOrder + Intent + Attempts + Fills).

### 요청
- Headers: `Authorization: Bearer <token>`
- Path: `upbitUuid` — Upbit 주문 UUID

### 응답 200 OK

```json
{
  "upbitUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "state": "done",
  "ordType": "price",
  "side": "BUY",
  "price": "95000000",
  "volume": "0.00526",
  "executedVolume": "0.00526",
  "intent": {
    "intentType": "ENTRY",
    "requestedKrw": "500000",
    "reasonCode": "SIGNAL_STRENGTH_HIGH"
  },
  "attempts": [
    {
      "attemptPublicId": "018e1234-5678-7000-8000-000000000002",
      "attemptNo": 1,
      "status": "ACKED",
      "upbitUuid": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
      "nextRetryAt": null,
      "errorCode": null,
      "errorMessage": null,
      "createdAt": "2026-03-06T03:45:01.000Z"
    }
  ],
  "fills": [
    {
      "tradeTime": "2026-03-06T03:45:02.000Z",
      "price": "95000000",
      "volume": "0.00526",
      "fee": "475"
    }
  ]
}
```

### 응답 404 Not Found
- upbitUuid에 해당하는 주문이 없을 때

```json
{
  "code": "ORDER_NOT_FOUND",
  "message": "주문을 찾을 수 없습니다.",
  "reasonCode": "UPBIT_UUID_UNKNOWN"
}
```

---

## 8. POST /api/v2/reconcile

Reconcile 트리거(운영용, 옵션).

UNKNOWN Attempt 확정을 위한 수동 reconcile 실행. `docs/architecture/order-pipeline.md` §7 참조.

### 요청
- Headers: `Authorization: Bearer <token>`
- Body(optional): `{ "scope": "full" | "open_orders_only" }` — 기본 full

### 응답 202 Accepted

```json
{
  "jobId": "018e1234-5678-7000-8000-00000000abcd",
  "status": "ACCEPTED",
  "message": "Reconcile 요청이 접수되었습니다."
}
```

### 주의
- reconcile은 레이트리밋 영향을 받으므로 과도 호출 금지
- UI에서 "재주문" 버튼 대신 "reconcile 트리거"만 제공

---

## 9. PUT /api/v2/markets/{market}

마켓 설정 수정(enabled, priority). FR-TRADE-001.

### 요청
- Headers: `Authorization: Bearer <token>`
- Path: `market` — 예: KRW-BTC
- Body(optional): `{ "enabled": boolean, "priority": number }` — 일부만 보내도 됨

### 응답 200 OK
- 수정된 마켓 정보(GET /api/v2/markets 항목과 동일 형식 1개)

---

## 10. POST /api/v2/markets/{market}/unsuspend

마켓 SUSPENDED 수동 해제. UNKNOWN 확정 실패 등으로 SUSPENDED된 마켓만 대상.

### 요청
- Headers: `Authorization: Bearer <token>`
- Path: `market` — 예: KRW-BTC

### 응답 200 OK
- 해제된 마켓의 `tradeStatus=ACTIVE`가 반영된 마켓 정보

### 주의
- 실수 방지를 위해 UI에서 확인(ConfirmDialog) 권장.

---

## 11. 에러 정책 및 UX 동작

### 11.1 표준 에러 본문

```json
{
  "code": "ERROR_CODE",
  "message": "사용자용 메시지",
  "reasonCode": "INTERNAL_REASON",
  "details": {}
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| code | string | 클라이언트 분기용 코드 |
| message | string | 사용자 노출 메시지 |
| reasonCode | string | 운영/디버깅용 상세 코드 |
| details | object | 추가 정보(선택) |

### 11.2 HTTP 상태별 동작

| HTTP | 의미 | UX 동작 |
|------|------|---------|
| **401** | 인증 없음/만료 | `POST /api/v2/auth/refresh` 1회 시도 → 성공 시 원 요청 1회 재시도, 실패 시 `/login` 리다이렉트 |
| **403** | 권한 없음(OWNER 아님) | 에러 카드 + "다른 계정은 차단됨" 안내, `/login` 유도 |
| **429** | 클라이언트→서버 레이트리밋 | "요청이 많습니다. 잠시 후 다시 시도해 주세요." + 재시도 지연(백오프) |
| **418** | Upbit 차단(서버 내부) | API 직접 반환은 드물고, dashboard/orders 응답의 `risk.blocked418Until`로 전달. 418 시 서버가 503 등으로 대체 반환 가능 |
| **5xx** | 서버 오류 | "일시적 오류입니다. 잠시 후 다시 시도해 주세요." + 재시도 가능(멱등 요청만) |

### 11.3 에러 응답 예시

**401 Unauthorized**
```json
{
  "code": "UNAUTHORIZED",
  "message": "인증이 만료되었습니다.",
  "reasonCode": "ACCESS_TOKEN_EXPIRED"
}
```

**403 Forbidden**
```json
{
  "code": "FORBIDDEN",
  "message": "이 계정은 접근할 수 없습니다.",
  "reasonCode": "NOT_OWNER"
}
```

**429 Too Many Requests**
```json
{
  "code": "RATE_LIMIT_EXCEEDED",
  "message": "요청이 많습니다. 잠시 후 다시 시도해 주세요.",
  "reasonCode": "CLIENT_RATE_LIMIT",
  "details": {
    "retryAfter": 60
  }
}
```

**503 Service Unavailable** (Upbit 418/일시 장애)
```json
{
  "code": "SERVICE_UNAVAILABLE",
  "message": "거래소 연동이 일시 중단되었습니다.",
  "reasonCode": "UPBIT_BLOCKED_418",
  "details": {
    "blockedUntil": "2026-03-06T05:00:00.000Z"
  }
}
```

**500 Internal Server Error**
```json
{
  "code": "INTERNAL_ERROR",
  "message": "일시적인 오류가 발생했습니다.",
  "reasonCode": "UNEXPECTED_ERROR"
}
```

### 11.4 프론트 처리 체크리스트
- [ ] 401 수신 시 refresh 1회 + 원 요청 1회 재시도
- [ ] 403 시 로그인 페이지 유도
- [ ] 429 시 사용자 안내 + 지수 백오프
- [ ] 5xx 시 재시도 가능 여부 판단(멱등만)
- [ ] `risk.blocked418Until` / `risk.unknownAttempts24h` / `risk.suspendedMarkets` 상단 배너 노출

---

## 12. Reason Code 참조

| reasonCode | 설명 |
|------------|------|
| ACCESS_TOKEN_EXPIRED | Access Token 만료 |
| NOT_OWNER | OWNER가 아닌 계정 로그인 |
| CLIENT_RATE_LIMIT | 클라이언트→서버 요청 과다 |
| UPBIT_BLOCKED_418 | Upbit 418 차단 |
| UPBIT_UUID_UNKNOWN | 주문 UUID 미존재 |
| RATE_LIMIT_429 | Upbit 429 (Attempt THROTTLED) |
| TIMEOUT | Upbit 요청 타임아웃 |
| UNEXPECTED_ERROR | 예기치 않은 서버 오류 |

---

## 13. 변경 이력

| 날짜 | 변경 내용 |
|------|----------|
| 2026-03-06 | 초안: dashboard, markets, strategy, orders, reconcile, 에러 정책 |
| 2026-03-06 | 엔드포인트 목록 확장: auth/refresh·logout, upbit/key, push, backtests, PUT markets, POST unsuspend. 마켓 수정·unsuspend 계약 추가. |
