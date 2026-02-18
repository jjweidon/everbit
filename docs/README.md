# Everbit v2 DOCS

Status: **Active**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

이 디렉터리는 everbit v2를 “문서 우선”으로 개발하기 위한 단일 소스 오브 트루스(Single Source of Truth)다.

프로젝트 진행 체크(로드맵/체크리스트): `roadmap.md`

권장 읽는 순서:
1) 요구사항(FRD/NFR)
2) 아키텍처(SoT)
3) 외부 연동(Upbit 등)
4) 테스트/성능
5) 운영/보안
6) 디자인 시스템(UI/UX)
7) 전략 스펙
8) ADR

핵심 SoT:
- 주문/멱등/재시도/레이트리밋: `architecture/order-pipeline.md`
- Event Bus/Queue(Outbox): `architecture/event-bus.md`
- Upbit 연동(레이트리밋/418/WS): `integrations/upbit.md`
- 푸시 알림(Web Push): `architecture/push-notifications.md`

참고(추후 P1+):
- Kafka 토픽/메시지(선택): `architecture/kafka-topics.md`

---

## 목차

### Roadmap
- [프로젝트 로드맵/완료 체크리스트](./roadmap.md)

### Requirements
- [기능 요구사항(FRD)](./requirements/functional.md)
- [비기능 요구사항(NFR)](./requirements/non-functional.md)

### Architecture
- [아키텍처 개요](./architecture/overview.md)
- [컴포넌트/모듈 경계](./architecture/components.md)
- [데이터 모델(테이블/인덱스/상태)](./architecture/data-model.md)
- [Spring Boot 코드/클래스 규칙](./architecture/spring-boot-conventions.md)
- [JPA 매핑 표준(복합키/공유PK)](./architecture/jpa-mapping.md)
- [DB 스키마/마이그레이션](./db/README.md)
- [v2 MVP 스키마 DDL(초안)](./db/schema-v2-mvp.sql)
- [Event Bus/Queue(Outbox/Queue)](./architecture/event-bus.md)
- [주문 파이프라인(멱등/재시도/레이트리밋)](./architecture/order-pipeline.md)
- [푸시 알림(Web Push)](./architecture/push-notifications.md)
- [Kafka 토픽/메시지(P1+ 선택)](./architecture/kafka-topics.md)

### Integrations
- [Upbit 연동 스펙(REST + WebSocket)](./integrations/upbit.md)

### Security
- [시크릿 관리](./security/secrets.md)
- [노출 정책(Public/Admin)](./security/exposure-policy.md)
- [위협 모델(최소)](./security/threat-model.md)

### Testing
- [TDD 개발 규칙(RED→GREEN→REFACTOR)](./testing/tdd.md)
- [테스트 전략](./testing/strategy.md)
- [테스트 매트릭스](./testing/test-matrix.md)
- [성능/검증 계획](./testing/performance-plan.md)

### Operations
- [환경 표준(local/prod)](./operations/environments.md)
- [OCI 세팅(Day-0)](./operations/oci-setup.md)
- [Nginx/TLS(HTTPS) 세팅](./operations/nginx-tls.md)
- [Admin Surface 정책(Grafana/Jenkins)](./operations/admin-surface-policy.md)
- [CI/CD (Jenkins)](./operations/ci-cd-jenkins.md)
- [알림/경보(Alerting) 정책](./operations/alerts.md)
- [배포 표준](./operations/deploy.md)
- [런북](./operations/runbook.md)
- [재해 복구(DR)](./operations/disaster-recovery.md)

### Design
- [UI/UX 디자인 컨셉(모던 다크)](./design/ui-ux-concept.md)

### Strategies
- [EXTREME_FLIP(극점포착)](./strategies/EXTREME_FLIP/spec.md)

### ADR(Architecture Decision Records)
- [ADR 목록](./adr/)