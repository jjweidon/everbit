# ADR-0007: 세션 전략 (Access Bearer + Refresh HttpOnly Cookie + Rotation)

Status: **Accepted**  
Date: 2026-02-15

---

## Context
v2는 Next.js(Vercel) + Spring Boot(API) 구조이며, 단일 테넌트이지만 실거래 기능을 포함한다.  
토큰 저장/전달 방식이 애매하면 CORS/CSRF/보안 이슈가 구현 단계에서 폭발한다.

---

## Decision
- Access Token:
  - 전달: `Authorization: Bearer <access>`
  - 수명: 짧게(예: 15분, 환경변수로 관리)
- Refresh Token:
  - 저장/전달: **HttpOnly + Secure 쿠키**
  - 수명: 길게(예: 14일, 환경변수로 관리)
  - **Rotation**: refresh 사용 시 새 refresh로 교체한다.
- 서버는 Redis에 `refresh_jti`를 저장하여 재사용 탐지(탈취 대응)를 한다.
- CORS는 `everbit.kr` 단일 origin allowlist로 고정한다.

---

## Consequences
- XSS에 대한 refresh 노출 위험이 낮아진다(HttpOnly).
- Cookie 기반이므로 CSRF 방어를 반드시 구성해야 한다(요청 헤더 토큰/Double submit 등, 구현에서 고정).
- 로컬 개발 시에도 동일한 전략을 유지해야 한다(secure cookie 옵션만 환경별 조정).

---

## Alternatives
- Refresh를 localStorage에 저장(XSS 리스크로 제외)
- 완전 서버 세션(쿠키 + 서버 세션만)(가능하지만 구현/운영 복잡도 증가)
