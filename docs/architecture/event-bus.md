# Event Bus / Queue (PostgreSQL Outbox/Queue) 스펙

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

v2 MVP에서는 Kafka를 **필수에서 제외**하고, PostgreSQL의 `outbox_event` 테이블을 **내구성 있는 이벤트 버스/큐**로 사용한다.

- **발행(Publish)**: 도메인 트랜잭션 안에서 `outbox_event` row를 INSERT
- **소비(Consume)**: 워커가 `SELECT ... FOR UPDATE SKIP LOCKED`로 row를 claim(점유) 후 처리
- **재시도/백오프**: `attempt_count`, `next_retry_at`, `last_error_*` 등 DB 컬럼으로 상태 관리
- **멱등/중복**: 기존 문서의 **DB 유니크 제약 + 상태 머신**을 그대로 사용
- **향후 Kafka 도입**: `EventBus` 인터페이스만 유지하고 구현체를 Kafka로 교체

이 문서는 “Kafka가 없을 때도 실거래에서 안전한 비동기 처리”를 위해 필요한 최소 규격을 고정한다.

---

## 1. 목표/비목표

### 1.1 목표
- 단일 VM, 저비용 운영에서 **운영 복잡도를 최소화**한다.
- 이벤트 전달은 **at-least-once**를 전제로 하고, 소비자는 **idempotent**로 동작한다.
- 도메인 변경(SoT)과 이벤트 발행을 **동일 DB 트랜잭션**으로 묶어 일관성을 확보한다.
- 추후 Kafka로 전환해도 비즈니스 로직은 그대로 유지되도록 **인터페이스 중심**으로 고정한다.

### 1.2 비목표(v2 MVP)
- 고빈도 시장 데이터(ticker/orderbook/trade)를 큐로 전달(금지)
- 정확히 한 번(exactly-once) 전달 보장
- 다중 테넌트/다중 컨슈머 그룹 수준의 복잡한 라우팅

---

## 2. 핵심 원칙(강제)

1) **SoT는 PostgreSQL**
- 정합성/멱등/상태 전이의 최종 기준은 DB다.

2) **EventBus/Queue는 at-least-once**
- 워커 크래시/재기동/타임아웃에 의해 동일 이벤트가 **중복 처리될 수 있다**.

3) **소비자는 idempotent**
- 중복 처리되어도 결과가 1회 처리와 동일해야 한다.
- 실거래 핵심(주문 생성 등)은 반드시 “DB 상태 머신”으로 방어한다.

4) **락은 짧게, 처리(외부 호출)는 트랜잭션 밖에서**
- `FOR UPDATE` 락을 잡은 채로 Upbit/WebPush 호출을 하면 DB가 멈춘다.
- **claim(점유) 트랜잭션은 짧게** 종료하고, 외부 호출은 별도 단계에서 수행한다.

---

## 3. 논리적 스트림(Stream) 정의

Kafka가 없더라도 “논리적 토픽”을 유지한다. DB에서는 `outbox_event.stream` 컬럼으로 구현한다.

| Stream | 목적 | 비고(추후 Kafka 도입 시) |
|---|---|---|
| `everbit.trade.command` | 주문 실행 커맨드 | Kafka topic 그대로 사용 가능 |
| `everbit.trade.event` | 주문/체결/포지션 이벤트 | Kafka topic 그대로 사용 가능 |
| `everbit.backtest.command` | 백테스트 job 커맨드 | |
| `everbit.backtest.event` | 백테스트 결과 이벤트 | |
| `everbit.system.event` | 설정/킬스위치/운영 이벤트 | compact 토픽 후보 |

---

## 4. 데이터 모델: outbox_event (필수)

> 최종 스키마는 `docs/architecture/data-model.md`가 SoT다. 여기서는 의미/동작을 정의한다.

### 4.1 outbox_event 컬럼 의미(권장 스키마)

- `event_id`는 **추적/중복 방지 키**로 사용한다.
- `status`, `attempt_count`, `next_retry_at`로 재시도/백오프를 DB에서 통제한다.

권장 컬럼:
- 식별
  - `id` (PK)
  - `event_id` (UUID, UNIQUE)
- 라우팅
  - `stream` (varchar)
  - `event_type` (varchar)
  - `aggregate_type`, `aggregate_id`
- 페이로드
  - `payload_json` (jsonb)
- 처리 상태
  - `status` (PENDING | PROCESSING | DONE | DEAD)
  - `attempt_count` (int)
  - `max_attempts` (int)
  - `next_retry_at` (timestamptz)
  - `last_attempt_at` (timestamptz)
- 락/복구
  - `locked_by` (varchar)
  - `locked_until` (timestamptz)
- 에러
  - `last_error_code` (varchar)
  - `last_error_message` (text)
- 타임스탬프
  - `created_at`, `updated_at`, `processed_at`

### 4.2 상태 머신(강제)

- `PENDING`: 처리 대기(또는 재시도 대기). `next_retry_at <= now()`인 row만 처리 대상.
- `PROCESSING`: 워커가 claim하여 처리 중. `locked_until`로 TTL 관리.
- `DONE`: 처리 완료(성공). 재처리 금지.
- `DEAD`: 영구 실패(수동 분석/재처리만).

전이 규칙:
- `PENDING → PROCESSING → DONE`
- `PENDING → PROCESSING → PENDING` (retryable failure)
- `PENDING → PROCESSING → DEAD` (non-retryable 또는 max_attempts 초과)
- `PROCESSING(스테일) → PENDING` (워커 크래시/타임아웃 복구)

---

## 5. 발행(Publish) 규격

### 5.1 트랜잭션 경계(강제)
도메인 변경과 outbox_event INSERT는 같은 DB 트랜잭션에서 수행한다.

예시:
- Signal 저장 + OrderIntent 저장 + `CreateOrderAttempt` 커맨드 outbox_event INSERT
- Attempt 상태 전이(ACKED 등) + `OrderAccepted` 이벤트 outbox_event INSERT

### 5.2 Event Envelope(공통)
Kafka 유무와 무관하게 envelope는 유지한다(추후 교체 비용 최소화).

```json
{
  "schemaVersion": 1,
  "eventId": "uuid",
  "type": "CreateOrderAttempt|OrderAccepted|OrderFilled|...",
  "occurredAt": "2026-02-17T00:00:00.000Z",
  "traceId": "uuid-or-w3c",
  "ownerId": "OWNER",
  "strategyKey": "EXTREME_FLIP",
  "market": "KRW-BTC",
  "payload": {}
}
```

저장 방식(권장):
- `outbox_event.event_id` = `eventId`
- `outbox_event.event_type` = `type`
- `outbox_event.payload_json` = envelope 전체 또는 payload + 메타 분리(둘 중 하나로 고정)

---

## 6. 소비(Consume) 규격

### 6.1 워커 모델
- 워커는 stream 단위로 polling/처리한다.
- 다중 워커(프로세스/스레드)에서 동시에 실행해도, `SKIP LOCKED`로 충돌 없이 분산 처리된다.

권장 워커(논리):
- `order-executor-worker` : `everbit.trade.command`
- `notification-worker` : `everbit.trade.event`
- `backtest-worker` : `everbit.backtest.command`
- `system-worker` : `everbit.system.event`

### 6.2 Claim(점유) 쿼리 패턴(강제)
핵심은 2가지다.
- `FOR UPDATE SKIP LOCKED`로 동시성 충돌 제거
- claim 트랜잭션을 짧게 종료(외부 호출은 밖에서)

권장 SQL(개념):

```sql
-- 1) 처리할 row를 "점유"(짧은 트랜잭션)
WITH candidate AS (
  SELECT id
  FROM outbox_event
  WHERE stream = :stream
    AND status = 'PENDING'
    AND next_retry_at <= now()
    AND (locked_until IS NULL OR locked_until < now())
  ORDER BY created_at
  LIMIT :batchSize
  FOR UPDATE SKIP LOCKED
)
UPDATE outbox_event e
SET status = 'PROCESSING',
    locked_by = :workerId,
    locked_until = now() + (:lockTtlSeconds || ' seconds')::interval,
    attempt_count = attempt_count + 1,
    last_attempt_at = now(),
    updated_at = now()
FROM candidate c
WHERE e.id = c.id
RETURNING e.*;
```

처리 흐름(강제):
1) 위 쿼리로 batch를 claim 후 **즉시 커밋**
2) claim된 row들을 처리(Upbit/WebPush 등)
3) 처리 결과에 따라 `DONE` 또는 `PENDING(next_retry_at 설정)` 또는 `DEAD`로 업데이트

### 6.3 스테일 락 복구(필수)
워커가 죽으면 `PROCESSING` 상태가 남는다. 복구는 다음 규칙으로 고정한다.

- `locked_until < now()` 인 `PROCESSING` row는 **재처리 가능**해야 한다.
- 복구 방식 2가지 중 하나로 고정한다.
  - (권장) claim 조건에서 `(locked_until IS NULL OR locked_until < now())`를 포함하여 자연 복구
  - (선택) 별도 sweeper가 주기적으로 `PROCESSING + locked_until expired`를 `PENDING`으로 되돌림

---

## 7. 재시도/백오프 규격

### 7.1 retryable / non-retryable 분류(강제)
- retryable: 일시적 장애(네트워크, 5xx, 타임아웃, 일시적 외부 의존 실패)
- non-retryable: 파라미터 오류(4xx), 정책 위반, 데이터 불일치(수동 개입 필요)

주의:
- **주문 생성(timeout/5xx)은 retryable로 즉시 재주문하지 않는다.**
  - `OrderAttempt`는 `UNKNOWN`으로 수렴하고 reconcile로 확정한다. (SoT: `docs/architecture/order-pipeline.md`)

### 7.2 백오프 계산(권장)
- base: 0.5~1s
- multiplier: 2
- max: 60s~300s(스트림별 상이)
- jitter: ±20% (동시 재시도 쏠림 방지)

DB 반영:
- 실패 시 `next_retry_at = now() + backoff`
- `attempt_count >= max_attempts`면 `DEAD`

### 7.3 DLT(Dead Letter) 처리(권장)
- `DEAD`는 자동 재처리 금지
- 운영자는 row를 확인하고:
  - 데이터 수정 후 `status=PENDING`, `attempt_count=0`, `next_retry_at=now()`로 수동 재처리
  - 또는 `DEAD` 유지(영구 보관/분석)

---

## 8. 멱등/중복 처리 규칙(필수)

### 8.1 주문 실행 커맨드(CreateOrderAttempt)
- 워커가 같은 커맨드를 중복 처리해도, `OrderAttempt` 상태 머신이 **no-op**으로 막아야 한다.
- 구현 표준:
  - Attempt가 이미 `ACKED/REJECTED/THROTTLED/UNKNOWN` 등 종결 상태면 즉시 종료
  - DB 유니크(예: `UNIQUE(order_intent_id, attempt_no)`, `UNIQUE(identifier)`)는 유지

### 8.2 푸시 알림(OrderAccepted)
- 푸시는 best-effort이고 중복 가능하다.
- 중복 방지(권장): `notification_log.event_id` UNIQUE로 1회만 전송되도록 만든다.

---

## 9. 관측 가능성(메트릭/로그)

### 9.1 필수 메트릭(권장 명세)
- backlog:
  - `everbit_outbox_pending{stream}` (gauge)
  - `everbit_outbox_processing{stream}` (gauge)
  - `everbit_outbox_dead{stream}` (gauge)
  - `everbit_outbox_oldest_pending_seconds{stream}` (gauge)
- 처리 성능:
  - `everbit_outbox_processed_total{stream,result}` (counter)
  - `everbit_outbox_handler_duration_seconds{stream,eventType}` (histogram)

### 9.2 로그 상관관계(필수)
- 모든 outbox_event 처리 로그는 다음을 포함한다.
  - `traceId`, `eventId`, `stream`, `eventType`, `aggregateType`, `aggregateId`

---

## 10. 향후 Kafka 도입 경로(고정)

### 10.1 인터페이스 유지
- 도메인/서비스는 `EventBus`에만 의존한다.
- 구현체만 교체한다.

예시(개념):
- `PostgresOutboxEventBus`: `outbox_event` INSERT
- `KafkaEventBus`: Kafka producer publish

### 10.2 소비 측 교체
- 현재: `OutboxWorker`가 DB polling → `EventDispatcher`로 핸들러 호출
- 미래: Kafka consumer → 동일 `EventDispatcher`로 핸들러 호출

즉, “핸들러/도메인 로직”은 변경하지 않고 “입출력 어댑터”만 변경한다.
