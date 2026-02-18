# Kafka 토픽 설계 (P1+ 선택)

Status: **Deferred (P1+)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

v2 MVP에서는 Kafka를 **사용하지 않는다**. 비동기 파이프라인은 PostgreSQL Outbox/Queue(`outbox_event`)로 구현한다.

- v2 MVP SoT: `docs/architecture/event-bus.md` (DB 기반 EventBus/Queue)
- Kafka 도입 결정: ADR-0009(채택), ADR-0004(P1+ 보류)

이 문서는 **P1+ 단계에서 Kafka를 도입할 경우**에 대비한 “토픽/메시지 계약”을 보관한다.

---

## 1. 기본 원칙(P1+)
- Kafka는 내부 비동기 파이프라인/버퍼다.
- 정합성의 기준은 PostgreSQL(SoT)이다.
- 시장 데이터 원본(ticker/trade/orderbook/candle 고빈도)은 Kafka로 흘리지 않는다.
- 전달은 **at-least-once**, 소비자는 **idempotent**로 구현한다.

---

## 2. 논리 스트림 ↔ Kafka 토픽 매핑

v2에서 사용하는 스트림 명칭은 Kafka 도입 시 그대로 topic으로 사용할 수 있도록 고정한다.

| Stream(DB) | Kafka Topic(P1+) | 목적 |
|---|---|---|
| `everbit.trade.command` | `everbit.trade.command` | 주문 실행 커맨드 |
| `everbit.trade.event` | `everbit.trade.event` | 주문/체결/포지션 이벤트 |
| `everbit.backtest.command` | `everbit.backtest.command` | 백테스트 job 요청 |
| `everbit.backtest.event` | `everbit.backtest.event` | 백테스트 결과 |
| `everbit.system.event` | `everbit.system.event` | 설정/킬스위치 |

---

## 3. 메시지 Envelope(공통)

Kafka 유무와 무관하게 envelope는 유지한다(교체 비용 최소화).

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

---

## 4. 토픽 목록(참고, P1+)

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

---

## 5. Outbox 패턴과 Kafka 연결(P1+)

Kafka를 도입하는 경우에도 “도메인 변경”과 “Kafka 발행”의 일관성을 위해 Outbox를 사용한다.

- 트랜잭션 내: 도메인 저장 + `outbox_event` row 생성
- Relay(별도 프로세스): outbox_event 읽기 → Kafka 발행 → DONE 마킹

주의:
- v2 MVP에서는 Relay 없이 **outbox_event 자체가 큐**다.
- P1+에서 Kafka를 도입하면 outbox_event는 “DB→Kafka 신뢰성”을 위한 outbox 역할로 변경될 수 있다.

---

## 6. Consumer Group(참고, P1+)

| Consumer Group | 구독 토픽 | 책임 |
|---|---|---|
| `order-executor` | `everbit.trade.command` | Attempt 실행(Upbit 호출), DB 업데이트, 이벤트 발행 |
| `notification-worker` | `everbit.trade.event` | OrderAccepted 시 WebPush 발송 + 구독 정리 |
| `backtest-worker` | `everbit.backtest.command` | 백테스트 실행/저장 |
| `system-listener` | `everbit.system.event` | KillSwitch/설정 반영 |

---

## 7. 실패/재시도(요약)

- THROTTLED(429): 자동 재시도 허용(새 Attempt)
- UNKNOWN(timeout/5xx): 자동 재주문 금지, reconcile 후 실패면 SUSPENDED
- DLT는 수동 분석만 허용

상세: `docs/architecture/order-pipeline.md`
