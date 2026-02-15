# Everbit v2 문서

Status: **Active**  
Last updated: 2026-02-15 (Asia/Seoul)

이 디렉터리는 everbit v2를 “문서 우선”으로 개발하기 위한 단일 소스 오브 트루스(Single Source of Truth)다.

- 요구사항(FRD/NFR) → 아키텍처(SoT) → 테스트/성능 → 운영/보안 → 전략 스펙 순으로 읽는다.
- **주문/멱등/재시도/레이트리밋**은 `architecture/order-pipeline.md`가 최종 기준이다.

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
- [배포 표준](./operations/deploy.md)
- [런북](./operations/runbook.md)
- [재해 복구(DR)](./operations/disaster-recovery.md)

### Strategies
- [EXTREME_FLIP(극점포착)](./strategies/EXTREME_FLIP/spec.md)

### ADR(Architecture Decision Records)
- [ADR 목록](./adr/)
