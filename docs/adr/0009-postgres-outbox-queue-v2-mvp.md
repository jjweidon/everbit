# ADR-0009: v2 MVP에서 Kafka 제외, PostgreSQL Outbox/Queue 채택

Status: **Accepted**  
Date: 2026-02-17

---

## Context
- v2 MVP는 1인 전용(싱글 테넌트)이며, 초기 운영 비용/복잡도를 최소화해야 한다.
- 기존 문서에서는 Kafka를 내부 비동기 파이프라인/버퍼로 사용하도록 설계했으나,
  - 단일 VM 환경에서 Kafka 운영(디스크, 모니터링, 장애 대응)이 추가 부담이 된다.
  - v2 MVP의 처리량/규모에서는 DB 기반 큐로도 충분하다.

---

## Decision
v2 MVP에서는 Kafka를 “필수”에서 제외하고, PostgreSQL의 `outbox_event` 테이블을 이벤트 버스/큐로 사용한다.

- 이벤트 발행: 도메인 트랜잭션 안에서 `outbox_event` INSERT
- 이벤트 소비: 워커가 `FOR UPDATE SKIP LOCKED`로 폴링/claim 후 처리
- 재시도/백오프: DB 컬럼(`attempt_count`, `next_retry_at`, `last_error_*`, `locked_until`)로 상태 관리
- 멱등/중복: 기존 설계의 **DB 유니크 제약 + 상태 머신**을 그대로 유지
- 향후 Kafka 도입: `EventBus` 인터페이스를 유지하고 구현체만 교체

SoT 문서:
- `docs/architecture/event-bus.md`
- `docs/architecture/order-pipeline.md`
- `docs/architecture/data-model.md`

---

## Consequences
### Positive
- 운영 구성 요소 감소(브로커 제거)로 장애 포인트/운영 부담 감소
- DB 트랜잭션과 이벤트 발행을 동일 트랜잭션으로 묶어 일관성 확보(Outbox)
- 구현 단순화로 MVP 개발 속도 향상

### Negative / Risks
- 큐 backlog가 DB에 쌓이므로, DB 디스크/IO 압박이 증가할 수 있다.
- 고처리량/고동시성 확장에는 불리하다(향후 Kafka 도입 여지 유지).
- 워커 폴링 방식이므로, 폴링 주기/배치/락 TTL 튜닝이 필요하다.

---

## Alternatives Considered
- Kafka Self-host (ADR-0004): v2 MVP 범위에서는 운영 부담이 더 크다고 판단하여 보류
- Managed Kafka: 예산 제약으로 제외
- Redis 기반 큐: 영속성/재처리/장애 시 정합성 설계 부담으로 제외
