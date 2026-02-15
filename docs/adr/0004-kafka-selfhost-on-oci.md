# ADR-0004: OCI 단일 VM 올인원 + Kafka Self-host

Status: **Accepted**  
Date: 2026-02-14

---

## Context
- 월 $8 미만 예산 제약
- 자동매매는 상시 실행이 필요
- 관리형 Kafka는 비용이 높아 예산 내 도입이 어렵다.

---

## Decision
- Oracle Cloud Free Tier(Always Free) 기반 **단일 VM 올인원** 구조를 채택한다.
- VM 내 Docker로 다음을 배치한다:
  - Nginx, Spring Boot, PostgreSQL, Redis, Kafka(KRaft), Prometheus/Grafana, Jenkins(선택)
- 가용성보다 **비용/단순성/통제**를 우선한다.

---

## Consequences
- 단일 장애점(SPOF)이다.
- 운영 부담(패치/백업/모니터링)이 증가한다.
- 예산 내에서 Kafka 포함 전체 스택 운영이 가능하다.

---

## Alternatives
- Managed Kafka(비용 초과 가능성 큼)
- 무료 PaaS(Idle sleep/재기동 지연으로 상시 워크로드에 부적합)
