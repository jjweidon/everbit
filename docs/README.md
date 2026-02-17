# Everbit v2 DOCS

Status: **Active**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

이 디렉터리는 everbit v2를 “문서 우선”으로 개발하기 위한 단일 소스 오브 트루스(Single Source of Truth)다.

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
- Kafka 토픽/메시지: `architecture/kafka-topics.md`
- Upbit 연동(레이트리밋/418/WS): `integrations/upbit.md`
- 푸시 알림(Web Push): `architecture/push-notifications.md`

---

## 목차

### Requirements
- [기능 요구사항(FRD)](./requirements/functional.md)
- [비기능 요구사항(NFR)](./requirements/non-functional.md)

### Architecture
- [아키텍처 개요](./architecture/overview.md)
- [컴포넌트/모듈 경계](./architecture/components.md)
- [데이터 모델(테이블/인덱스/상태)](./architecture/data-model.md)
- [Kafka 토픽/메시지](./architecture/kafka-topics.md)
- [주문 파이프라인(멱등/재시도/레이트리밋)](./architecture/order-pipeline.md)
- [푸시 알림(Web Push)](./architecture/push-notifications.md)

### Integrations
- [Upbit 연동 스펙(REST + WebSocket)](./integrations/upbit.md)

### Security
- [시크릿 관리](./security/secrets.md)
- [노출 정책(Public/Admin)](./security/exposure-policy.md)
- [위협 모델(최소)](./security/threat-model.md)

### Testing
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
