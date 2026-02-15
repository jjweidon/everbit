# ADR-0006: Admin Surface 비공개(기본) + SSH 터널 접근

Status: **Accepted**  
Date: 2026-02-14

---

## Context
Grafana/Jenkins/Prometheus 같은 Admin Surface를 인터넷에 직접 노출하면 공격면이 급증한다.  
v2는 단일 VM 올인원이고 1인 운영이므로 “안전한 기본값”이 필요하다.

---

## Decision
- Admin Surface는 **기본 비공개**로 고정한다.
- 운영 compose(prod)에서 Admin 포트는 외부 publish 금지, 필요 시 **127.0.0.1에만 publish**한다.
- 접근은 SSH 터널이 표준이다.
- 예외 공개는 Break-glass로만 허용하며 기간/사유/allowlist/인증이 강제다.

---

## Consequences
- 운영 편의는 다소 떨어지지만, 공격면을 크게 줄인다.
- 접근 방법이 표준화되어 운영 실수를 줄인다.

---

## Alternatives
- 서브도메인으로 상시 공개(편하지만 위험)
- VPN/ZeroTrust 도입(P1 이후 고려 가능)
