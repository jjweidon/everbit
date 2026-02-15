# ADR 0006: Admin Surface 비공개(기본) + SSH 터널 접근

- Status: **Accepted**
- Date: 2026-02-14

## Context

Everbit v2는 단일 VM 올인원 구성으로 시작한다. 이 구성에서 Grafana/Jenkins/Prometheus 같은 운영 도구를 인터넷에 직접 노출하면:

- 공격면(Attack Surface)이 급격히 증가하고
- 플러그인/의존성 취약점의 영향이 커지며
- 단일 VM 장애/침해 시 피해 범위가 전체 시스템으로 확장된다.

또한 v2는 **1인 전용** 서비스이므로, 다수 운영자/조직 협업을 위한 “외부 공개 운영 콘솔” 요구가 없다.

## Decision

1) Admin Surface(Grafana/Jenkins/Prometheus/DB/Redis/Kafka)는 **기본 비공개**로 고정한다.
2) 접근은 **SSH 터널**을 표준으로 한다.
3) 예외적으로 외부 공개가 필요할 경우에도, 다음을 모두 만족해야 한다.
   - Nginx 443(TLS) 뒤로만 노출
   - 별도 서브도메인 사용
   - IP allowlist + 인증(기본 인증/SSO 등)
   - 공개 기간/사유를 런북에 기록하고 종료 즉시 원복

관련 스펙 문서:
- `docs/operations/admin-surface-policy.md`
- `docs/security/exposure-policy.md`

## Consequences

- 운영 도구 침해/스캔으로 인한 리스크를 크게 낮춘다.
- 원격 운영은 SSH 터널로 가능하므로 기능적 손실이 없다.
- “예외 공개”는 비용/복잡도를 증가시키므로, 반드시 기록/통제한다.

## Alternatives Considered

1) Grafana/Jenkins를 인터넷에 상시 공개
- 운영 편의는 올라가지만, 보안 리스크가 과도하게 증가한다.

2) VPN/Bastion 도입으로 22까지 닫기
- 보안은 좋아지지만, Always Free 단일 VM 구조에서는 네트워크 구성/운영 복잡도가 상승한다.
- 추후 필요해지면 별도 ADR로 도입한다.
