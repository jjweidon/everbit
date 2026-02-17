# 비기능 요구사항 (NFR)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

이 문서는 “운영에서 깨지지 않기 위한” 최소 기준을 고정한다.  
세부 구현 스펙은 `docs/architecture/*.md`, 운영 절차는 `docs/operations/*.md`가 최종 기준이다.

---

## 1. 보안(Security)

### 1.1 시크릿 관리
- 시크릿은 Git 커밋 금지(.gitignore), AI 인덱싱 금지(.cursorignore)
- 운영 시크릿은 VM 환경변수 또는 root-only 파일(`/etc/everbit/everbit.env`, 600)로만 주입한다.
- 업비트 키는 DB에 암호문으로만 저장하고, 암호화 마스터키는 운영 환경변수로만 제공한다. (ADR-0005)

### 1.2 인증/세션
- Access Token: Bearer header
- Refresh Token: HttpOnly + Secure cookie
- Refresh rotation + 재사용 탐지(서버 Redis `refresh_jti`)
- Refresh Cookie 속성(결정 고정):
  - `HttpOnly`, `Secure`, `SameSite=Lax`
  - `Path`는 refresh 전용으로 좁힌다(예: `/api/v2/auth/refresh`).
- CSRF 방어(필수):
  - refresh/logout 등 **cookie 기반** 엔드포인트는 `Origin`/`Referer` allowlist 검증을 강제한다(`https://everbit.kr`만).
  - (선택, P1) Double Submit CSRF 토큰 도입(비-HttpOnly 쿠키 + 헤더).
- CORS는 `everbit.kr`(프론트) → `api.everbit.kr`(백)만 허용한다(명시 allowlist).

### 1.3 노출 정책
- 외부 공개 포트는 원칙적으로 80/443만 허용한다.
- Admin Surface(Grafana/Jenkins/Prometheus/DB/Redis/Kafka)는 기본 비공개이며 SSH 터널이 표준이다. (ADR-0006)
- `/actuator/**`는 외부 노출 금지(내부 스크랩만).

---

## 2. 신뢰성/장애 대응(Reliability)

### 2.1 DB가 SoT, Kafka는 at-least-once
- 정합성의 기준은 PostgreSQL이며, Kafka는 전달/버퍼 역할이다.
- 소비자는 중복 수신을 전제로 idempotent하게 구현한다.

### 2.2 중복 주문 방지(필수)
- 멱등은 **DB 유니크 제약 + 상태 머신**으로 보장한다.
- Upbit identifier는 재사용 금지이며 멱등키로 사용하지 않는다.

### 2.3 UNKNOWN 수렴(필수)
- timeout/네트워크/5xx 등 “생성 여부 불확실”은 자동 재주문으로 해결하지 않는다.
- Attempt는 UNKNOWN으로 기록하고, reconcile 후에도 확정 불가 시 시장 단위 SUSPENDED로 안전 중단한다.

### 2.4 레이트리밋/차단(필수)
- 429: 해당 그룹 즉시 중단 + 백오프 + 이후 새 Attempt로 재시도
- 418(차단): 차단 해제 시각까지 재시도 금지 + 자동매매 중단(운영 경고)

---

## 3. 성능(Performance)

### 3.1 실거래 경로
- 주문 생성 경로는 레이트리밋에 의해 상한이 걸리므로, 병렬성을 과도하게 올리지 않는다.
- 시스템은 429 상황에서도 다운되지 않고, 큐 적체로 degrade 되어야 한다.

### 3.2 백테스트 경로
- 멀티 마켓/멀티 타임프레임 병렬 처리를 지원하되, DB IO/CPU 병목을 측정 기반으로 조절한다.
- 최소 목표:
  - 10 markets × 3 timeframes × 365일 데이터가 “현실적인 시간” 안에 완료(정확한 숫자는 `testing/performance-plan.md`에 고정).

---

## 4. 관측 가능성(Observability)

### 4.1 메트릭(필수)
- 주문: 성공/실패/UNKNOWN/THROTTLED(429)/BLOCKED(418) 카운트
- 레이트리밋: group별 429/418 카운트, Remaining-Req 관측(가능한 범위)
- 전략: 마켓별 신호 빈도, 포지션 수, PnL
- 인프라: JVM 메모리/GC, DB 커넥션, Kafka lag

### 4.2 로그(필수)
- correlation id(traceId)로 API ↔ Kafka ↔ DB 이벤트를 연결한다.
- 민감정보는 로그에 기록하지 않는다(키/토큰/쿠키/암호문/개인정보).

---

## 5. 운영성(Operability)

### 5.1 배포/롤백
- 운영 배포는 Git SHA 기준으로 추적 가능해야 한다.
- 롤백 절차는 `docs/operations/deploy.md`에 고정한다.

### 5.2 백업/복구
- Postgres 논리 백업(pg_dump) 최소 하루 1회, 7~14일 보관
- DR 목표(RPO/RTO)는 `docs/operations/disaster-recovery.md`에 고정한다.

### 5.3 Admin Surface 접근
- 기본: SSH 터널
- 예외 공개는 기간/사유/allowlist/인증이 강제이며, 종료 후 즉시 원복한다.

---

## 6. 푸시 알림(신뢰성/보안)

- 푸시 알림은 “best effort”이며 거래/정합성의 SoT가 아니다.
- 푸시 실패(만료/해지)는 자동으로 구독을 정리한다.
- VAPID private key는 운영 시크릿이며 외부 노출/로그 출력 금지.
