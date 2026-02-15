# ADR-0004: OCI 단일 VM 올인원 + Kafka Self-host

- Status: **Accepted**
- Date: 2026-02-14

## Context
v2는 월 $8 미만 예산 제약이 있고, 자동매매는 상시 실행이 필요하다.  
관리형 Kafka는 비용이 높아 예산 내 도입이 어렵다.

## Decision
- Oracle Cloud Free Tier(Always Free) 기반 **단일 VM 올인원** 구조를 채택한다.
- VM 내에 Docker로 다음 컴포넌트를 배치한다:
  - Nginx (reverse proxy)
  - Spring Boot 서버
  - PostgreSQL
  - Redis
  - Kafka (KRaft 단일 노드)
  - Prometheus + Grafana
  - Jenkins (필요 시)
- 가용성보다 **비용/단순성/통제**를 우선한다.

## Consequences
- Kafka/DB/모니터링이 모두 한 VM에 있으므로 단일 장애점(SPOF)이다.
- 운영 부담(패치/백업/모니터링)이 증가한다.
- 예산 내에서 Kafka를 포함한 전체 스택을 유지할 수 있다.

## Alternatives Considered
1) Managed Kafka(Confluent/Aiven 등)  
- 단점: 비용이 예산 제약을 초과하는 경우가 대부분

2) 무료 PaaS(Render 등)  
- 단점: idle sleep/재기동 지연으로 상시 실행 워크로드에 부적합
