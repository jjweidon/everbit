# 운영/노출 정책 (Public Surface / Admin Surface)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- 인터넷에 노출되는 공격면(Attack Surface)을 최소화한다.
- 단일 VM 올인원 구조에서도 “최소 포트/최소 경로/최소 권한” 원칙을 강제한다.

---

## 1. 기본 원칙(강제)

1) **Public Surface는 API만**
- 외부 공개는 `api.everbit.kr`(Nginx 443)만 허용한다.

2) **Admin Surface는 기본 비공개**
- Grafana/Jenkins/Prometheus/DB/Redis/Kafka는 외부 노출 금지.
- 접근은 SSH 터널이 표준이며, 예외 공개는 기간/사유/allowlist/인증이 강제다.

3) **두 단계 방화벽**
- 1차: OCI NSG/Security List
- 2차: OS iptables + docker publish 최소화

---

## 2. 포트 정책

### 2.1 반드시 열어야 하는 포트
- 80/tcp: Let's Encrypt HTTP-01 + HTTPS redirect
- 443/tcp: API

### 2.2 SSH
- 22/tcp: Source CIDR를 본인 IP(/32)로 제한(필수)
- 비밀번호 로그인 금지, 키 인증만 허용

### 2.3 절대 열지 않는 포트(고정)
- 5432(Postgres), 6379(Redis), 9092(Kafka), 9090(Prometheus), 3000(Grafana), Jenkins port 등
- 이유: 스캐닝/취약점/크리덴셜 공격에 직접 노출

---

## 3. Nginx(Edge) 보안 규칙

- HTTP → HTTPS 강제
- `/actuator/**` 외부 차단(내부 스크랩만)
- 기본 Rate limit(스캔/봇 방어)
- 불필요한 메서드 제한(예: TRACE 차단)

---

## 4. Admin 도구 접근
- 기본: SSH 터널
- 예외 공개: `docs/operations/admin-surface-policy.md`의 Break-glass 절차 필수

---

## 5. 참고
- OCI Security Rules: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm
