# ADR-0004: OCI 단일 VM 올인원 + Kafka Self-host (P1+)

Status: **Deferred (P1+)**  
Date: 2026-02-14  
Updated: 2026-02-17

---

## Context
- 월 $8 미만 예산 제약
- 자동매매는 상시 실행이 필요
- 관리형 Kafka는 비용이 높아 예산 내 도입이 어렵다.

추가 맥락(v2 MVP 변경):
- v2 MVP에서는 Kafka를 “필수”에서 제외하고 PostgreSQL Outbox/Queue(`outbox_event`)로 대체한다. (ADR-0009)
- Kafka는 처리량/확장 또는 컴포넌트 분리(P1+) 단계에서 도입 후보로 유지한다.

---

## Decision
- Oracle Cloud Free Tier(Always Free) 기반 **단일 VM 올인원** 구조를 채택한다.
- Kafka를 도입하는 시점(P1+)에는 VM 내 Docker로 다음을 배치한다:
  - Nginx, Spring Boot, PostgreSQL, Redis, Kafka(KRaft), Prometheus/Grafana, Jenkins(선택)
- 가용성보다 **비용/단순성/통제**를 우선한다.

---

## Consequences
- 단일 장애점(SPOF)이다.
- 운영 부담(패치/백업/모니터링)이 증가한다.
- (Kafka 도입 시) 예산 내에서 Kafka 포함 전체 스택 운영이 가능하다.

---

## Alternatives
- v2 MVP: PostgreSQL Outbox/Queue 채택(ADR-0009)
- Managed Kafka(비용 초과 가능성 큼)
- 무료 PaaS(Idle sleep/재기동 지연으로 상시 워크로드에 부적합)
