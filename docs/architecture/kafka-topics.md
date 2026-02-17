# Kafka 토픽 설계 (트레이딩 이벤트 최소화)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

원칙:
- Kafka는 내부 비동기 파이프라인/버퍼다.
- 정합성의 기준은 PostgreSQL(SoT)이다.
- 시장 데이터 원본(고빈도)은 Kafka로 흘리지 않는다.

---

## 1. 운영 전제
- 단일 VM, Kafka 단일 브로커(KRaft)
- Replication factor = 1 (HA는 v2 범위 밖)
- 전달은 **at-least-once**, 소비자는 **idempotent**로 구현한다.

---

## 2. 토픽 설계 원칙

### 2.1 Kafka로 보내지 않는 것
- ticker/trade/orderbook/candle 원본 스트림(고빈도)

### 2.2 Kafka로 보내는 것(최소)
- 주문 실행 커맨드(Attempt 실행)
- 주문/체결/포지션 이벤트(대시보드/알림 트리거)
- 백테스트 job 커맨드/결과
- Kill Switch/설정 변경 이벤트

---

## 3. Outbox 패턴(필수)

DB 트랜잭션에서 “도메인 변경”과 “Kafka 발행”의 일관성을 위해 Outbox를 사용한다.

- 트랜잭션 내: 도메인 저장 + outbox row 생성
- Outbox Relay: outbox 읽기 → Kafka 발행 → SENT 마킹

---

## 4. 토픽 목록(MVP)

| Topic | 목적 | Key | Partitions | Cleanup | Retention |
|---|---|---|---:|---|---|
| `everbit.trade.command` | 주문 실행 커맨드 | `market` | 8 | delete | 7d |
| `everbit.trade.event` | 주문/체결/포지션 이벤트 | `market` | 8 | delete | 14d |
| `everbit.backtest.command` | 백테스트 job 요청 | `jobId` | 1 | delete | 14d |
| `everbit.backtest.event` | 백테스트 결과 | `jobId` | 1 | delete | 30d |
| `everbit.system.event` | 설정/킬스위치 상태 | `ownerId` | 1 | compact | ∞ |
| `everbit.dlt` | 실패 메시지(수동 분석) | `topic+key` | 1 | delete | 30d |

정책:
- DLT는 자동 재처리 금지(실거래 시스템에서 자동 반복은 위험).
- trade.command retention은 “장애 분석”을 위해 3d보다 길게(기본 7d) 유지한다.

---

## 5. 메시지 Envelope(공통)

```json
{
  "schemaVersion": 1,
  "eventId": "uuid",
  "type": "CreateOrderAttempt|OrderAccepted|OrderFilled|...",
  "occurredAt": "2026-02-15T00:00:00.000Z",
  "traceId": "uuid-or-w3c",
  "ownerId": "OWNER",
  "strategyKey": "EXTREME_FLIP",
  "market": "KRW-BTC",
  "payload": {}
}
```

- `eventId`: dedupe/추적
- `traceId`: 로그/메트릭 연계
- `schemaVersion`: 변경 시 하위 호환 규칙 필요

---

## 6. 커맨드(Command)

### 6.1 CreateOrderAttempt (핵심)
원칙:
- Kafka 메시지는 “Attempt를 실행하라”만 전달한다.
- Upbit 요청 파라미터(side/ord_type/identifier 등)는 **DB(OrderAttempt)의 스냅샷**을 사용한다.

payload:
```json
{
  "orderAttemptId": 123456,
  "orderIntentId": 23456,
  "intentType": "ENTRY|EXIT_STOPLOSS|EXIT_TAKEPROFIT|EXIT_TRAILING|EXIT_TIME"
}
```

### 6.2 CancelOrderAttempt (선택, P0에서는 제한적으로)
payload:
```json
{
  "orderId": 777,
  "upbitUuid": "uuid-if-known"
}
```

### 6.3 SyncOrder / SyncBalance (저빈도)
- WebSocket 끊김/재연결/부팅 시 reconcile 트리거 용도
- 호출 빈도는 낮게 유지(예: 1~5분 단위)

---

## 7. 이벤트(Event)

필수 이벤트:
- `OrderAccepted`: Upbit 주문 생성 ACK(Upbit UUID 확보)
- `OrderRejected`: 주문 생성 실패(비즈니스/권한/파라미터)
- `OrderThrottled`: 429로 THROTTLED 종료
- `OrderUnknown`: timeout/5xx로 UNKNOWN 수렴
- `OrderFilled`: 체결(부분/전체)
- `PositionUpdated`: 포지션/평단/수량 갱신
- `PnlUpdated`: 손익 갱신
- `KillSwitchChanged`: 계정/전략 스위치 변경

푸시 알림 트리거:
- `OrderAccepted` 이벤트를 notification-worker가 소비하여 WebPush 발송(FRD: FR-NOTI-001)
- 상세: `docs/architecture/push-notifications.md`

---

## 8. Consumer Group(MVP)

| Consumer Group | 구독 토픽 | 책임 |
|---|---|---|
| `order-executor` | `everbit.trade.command` | Attempt 실행(Upbit 호출), DB 업데이트, 이벤트(outbox) |
| `trade-projector` | `everbit.trade.event` | (선택) UI용 read-model/캐시 갱신 |
| `notification-worker` | `everbit.trade.event` | OrderAccepted 시 WebPush 발송 + 구독 정리 |
| `backtest-worker` | `everbit.backtest.command` | 백테스트 실행/저장 |
| `system-listener` | `everbit.system.event` | KillSwitch/설정 반영 |

---

## 9. 실패/재시도(요약)

- THROTTLED(429): 자동 재시도 허용(새 Attempt)
- UNKNOWN(timeout/5xx): 자동 재주문 금지, reconcile 후 실패면 SUSPENDED
- DLT는 수동 분석만 허용

상세: `docs/architecture/order-pipeline.md`
