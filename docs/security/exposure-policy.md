# 운영/노출 정책 (Public Surface / Admin Surface)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

- 인터넷에 노출되는 공격면(Attack Surface)을 최소화한다.
- 단일 VM 올인원 구조에서도 “최소 포트/최소 경로/최소 권한” 원칙을 적용한다.

---

## 2. 기본 원칙(고정)

1) **Public Surface는 API만**
- 외부 공개는 `api.everbit.kr`(Nginx 443)만 허용한다.

2) **Admin Surface는 기본 비공개**
- Grafana/Jenkins/Prometheus/DB/Redis/Kafka는 외부 노출 금지.
- 접근은 SSH 터널 또는 제한된 allowlist를 사용한다.

3) **두 단계 방화벽**
- 1차: OCI NSG/Security List
- 2차: OS iptables(및 docker publish 최소화)

---

## 3. 네트워크/포트 정책

### 3.1 반드시 열어야 하는 포트
- 80/tcp: Let's Encrypt HTTP-01 + HTTPS redirect
- 443/tcp: API

### 3.2 SSH
- 22/tcp: Source CIDR를 본인 IP로 제한(필수)
- 비밀번호 로그인 금지, 키 인증만

### 3.3 절대 열지 않는 포트
- 5432, 6379, 9092, 9090, 3000, Jenkins port 등
- 이유: 외부 공개 시 취약점/스캐닝에 직접 노출

---

## 4. Nginx(Edge) 보안 정책

- HTTP → HTTPS 강제
- `/actuator/**` 외부 차단(내부 스크랩만)
- 기본 Rate limit(스캔/봇 방어)
- 불필요한 메서드 제한(예: TRACE 차단)

---

## 5. 운영 도구 노출 정책

- Grafana/Jenkins 접근은 `docs/operations/admin-surface-policy.md`를 따른다.
- “예외 공개”는 반드시 기간/사유를 남기고, 종료 시 즉시 닫는다.

---

## 6. 레퍼런스

- OCI Security Rules: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securityrules.htm
- Security Lists: https://docs.oracle.com/en-us/iaas/Content/Network/Concepts/securitylists.htm
