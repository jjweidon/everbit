# Kafka 토픽 설계 (트레이딩 이벤트 최소화)

## 1. 목적

Everbit v2는 Kafka를 **내부 비동기 파이프라인/버퍼**로 사용한다.

- 주문 실행을 “DB 트랜잭션”과 분리하여 신뢰성(재시도/재기동) 확보
- 백테스트 등 장시간 작업을 Job 큐로 분리
- 모듈 간 결합도 감소

> 단, Kafka는 이벤트 소싱의 소스 오브 트루스가 아니다. **정합성의 기준은 PostgreSQL**이다.

---

## 2. 운영 전제(초기)

- 단일 VM, Kafka 단일 브로커
- KRaft 모드(컨트롤러/브로커 일체)
- Replication factor = 1 (가용성은 v2 범위 밖)
- 메시지 전달은 **at-least-once**로 가정하고, 소비자는 **idempotent**하게 구현한다.

---

## 3. “트레이딩 이벤트 최소화” 원칙

### 3.1 Kafka로 흘리지 않는 것
- Ticker, Trade, Orderbook, Candle 원본 스트림(= 고빈도 시장 데이터)

시장 데이터는 WebSocket에서 수신해 **서버 메모리/Redis**에서 처리하고,
Kafka에는 **의사결정 결과/상태 변화만** 기록한다.

### 3.2 Kafka에 남기는 것(최소)
- 주문 파이프라인 커맨드: `CreateOrder`, `CancelOrder`, `SyncOrder`, `SyncBalance`
- 주문 라이프사이클 이벤트: `OrderAccepted`, `OrderRejected`, `OrderFilled`, `OrderCanceled`
- 포지션/손익 상태 변화 이벤트(대시보드 갱신용): `PositionUpdated`, `PnlUpdated`
- 백테스트 Job 커맨드/결과
- Kill Switch 상태 변경

---

## 4. Outbox 패턴(필수)

DB 트랜잭션에서 “도메인 변경”과 “Kafka 발행”을 원자적으로 묶기 위해 Outbox를 적용한다.

- 트랜잭션 내에서:
  - OrderIntent 저장
  - outbox row 생성
- 별도 Outbox Relay(스케줄러/워커)가:
  - outbox row를 읽어 Kafka 발행
  - 성공 시 outbox를 `SENT`로 마킹

장점:
- 서버 재기동 시에도 메시지 유실/중복을 통제 가능
- Kafka 장애 시 DB가 버퍼 역할

---

## 5. 토픽 목록(초기 MVP)

> 파티션은 “멀티 마켓 동시 실행”을 고려해, **마켓 키 기반 정렬**이 가능하도록 설계한다.

| Topic | 목적 | Key | Partitions | Cleanup | Retention |
|------|------|-----|-----------:|---------|----------|
| `everbit.trade.command` | 주문/동기화 커맨드 | `market` | 8 | delete | 3d |
| `everbit.trade.event` | 주문/체결/포지션 이벤트 | `market` | 8 | delete | 7d |
| `everbit.backtest.command` | 백테스트 Job 요청 | `jobId` | 1 | delete | 7d |
| `everbit.backtest.event` | 백테스트 결과 | `jobId` | 1 | delete | 30d |
| `everbit.system.event` | KillSwitch/설정 변경 | `owner` | 1 | compact | ∞ |
| `everbit.dlt` | 실패 메시지(수동 분석) | `topic+key` | 1 | delete | 14d |

설명:
- `everbit.trade.*`는 partition=8로 시작(OCI A1에서도 무리 없는 수준).
- `everbit.system.event`는 최신 상태만 있으면 되므로 compaction.
- DLT는 **자동 재처리 하지 않는다**(실거래 시스템에서 자동 반복은 위험).

---

## 6. 메시지 포맷(공통 Envelope)

JSON을 기본으로 하고, 버전/추적을 고정한다.

```json
{
  "schemaVersion": 1,
  "eventId": "uuid",
  "type": "CreateOrder|OrderAccepted|OrderFilled|...",
  "occurredAt": "2026-02-14T12:34:56.789Z",
  "traceId": "w3c-trace-id-or-uuid",
  "ownerId": "single-tenant-owner",
  "strategyKey": "EXTREME_FLIP",
  "market": "KRW-BTC",
  "payload": { }
}
```

- `schemaVersion`: 호환성 관리(변경 시 소비자 호환 규칙 필수)
- `traceId`: 로그/메트릭 연계
- `ownerId`: 단일 테넌트지만, 메시지 레벨에서 고정(확장성/안전)

---

## 7. 커맨드 타입 정의

### 7.1 CreateOrder (핵심)
payload 예시:

```json
{
  "orderIntentId": 12345,
  "side": "BID|ASK",
  "ordType": "LIMIT|MARKET|PRICE|BEST",
  "price": "100000",
  "volume": "0.01",
  "identifier": "EF:KRW-BTC:1m:20260214T123500Z:LONG"
}
```

- `identifier`는 Upbit 주문 생성 시 전달하는 사용자 지정 주문 ID.
- identifier는 계정 내에서 유일해야 하며, **재사용 불가**이므로 생성 규칙을 엄격하게 고정한다.  
  (생성 규칙은 `docs/architecture/order-pipeline.md` 참조)

### 7.2 CancelOrder
```json
{
  "orderId": 777,
  "upbitUuid": "uuid-if-known",
  "identifier": "..."
}
```

### 7.3 SyncOrder / SyncBalance
- WebSocket이 끊기거나 정합성 검사 시 사용
- 호출 빈도를 매우 낮게 유지(예: 1~5분 단위)

---

## 8. 이벤트 타입 정의

- `OrderAccepted`: Upbit 주문 생성 성공(Upbit UUID 확보)
- `OrderRejected`: Upbit 주문 생성 실패
- `OrderFilled`: 체결 발생(부분/전체)
- `OrderCanceled`: 취소 완료
- `PositionUpdated`: 평균단가/수량/노출 업데이트
- `PnlUpdated`: 손익 지표 업데이트
- `KillSwitchChanged`: 계정/전략 스위치 변경

---

## 9. 소비자(Consumer) 그룹 구성(초기)

| Consumer Group | 구독 토픽 | 책임 |
|---|---|---|
| `order-executor` | `everbit.trade.command` | Create/Cancel/Sync 실행(Upbit 호출), DB 업데이트, 이벤트 발행(outbox) |
| `trade-event-projector` | `everbit.trade.event` | (선택) UI용 ReadModel 캐시/Redis 반영 |
| `backtest-worker` | `everbit.backtest.command` | 백테스트 실행, 결과 저장, 결과 이벤트 발행 |
| `system-listener` | `everbit.system.event` | KillSwitch 상태를 메모리/Redis로 반영 |

> 단일 프로세스 내 모듈로 구현하더라도, “토픽/그룹” 기준을 유지하면 나중에 프로세스 분리할 때 비용이 낮다.

---

## 10. 실패/재시도 정책(요약)

- `order-executor`:
  - Upbit 429(레이트리밋) 발생 시: 즉시 해당 group 호출 중단 후 백오프  
  - 네트워크/5xx: 지수 백오프 + 최대 N회 재시도
  - 비즈니스 오류(파라미터, 잔고부족 등): 재시도 금지, 바로 `OrderRejected`
- 재시도는 **항상 DB 상태 기반으로 idempotent**하게 수행한다.

상세: `docs/architecture/order-pipeline.md`

---

## 11. 운영 파라미터(초기 권장)

- `max.poll.records`: 10~50
- Consumer concurrency:
  - `everbit.trade.command`: 1~2(레이트리밋 때문에 과도한 병렬 금지)
- Producer:
  - `acks=all`은 단일 브로커에선 의미 제한적이지만, 발행 성공 여부는 엄격히 체크
  - Outbox로 “최종 전달”을 보장한다.

---

## 12. 레퍼런스

- Upbit 주문 생성에서 identifier 사용/재사용 금지, 주문 생성 그룹 Rate Limit: https://docs.upbit.com/kr/reference/new-order
- Upbit Rate Limit과 Remaining-Req 헤더: https://docs.upbit.com/kr/reference/rate-limits
