# Everbit v2 아키텍처 개요 (단일 VM 올인원)

## 1. 목적

Everbit v2는 **1인 전용(싱글 테넌트)** 자동매매 시스템이다. 초기 운영 비용을 최소화하기 위해 **단일 VM(OCI Free Tier 우선) 올인원** 배포를 기본으로 한다.

이 문서는 다음을 고정한다.

- 런타임 구성요소(Nginx/Spring/PostgreSQL/Redis/Kafka/Prometheus/Grafana/Jenkins)
- 네트워크/보안 경계
- 주요 데이터 플로우
- 운영 및 확장 관점의 제약사항

> 전략(EXTREME_FLIP) 상세 요구사항은 `docs/strategies/EXTREME_FLIP/spec.md`에서 정의한다.

---

## 2. 시스템 컨텍스트

- **Client**: Next.js (Vercel)
- **API**: Spring Boot 서버 (단일 VM)
- **외부 연동**
  - Kakao OAuth2 (로그인 단일 수단)
  - Upbit Open API (주문/잔고/조회)
  - Upbit WebSocket (시세 + private: myOrder/myAsset)

권장 도메인 구성:

- `everbit.kr` : 프론트엔드(Vercel)
- `api.everbit.kr` : 백엔드(API Gateway = Nginx → Spring)
- (옵션) `jenkins.everbit.kr` : Jenkins UI (**기본 비공개**. 필요 시 SSH 터널/allowlist로만 접근, ADR 0006)

---

## 3. 배포 토폴로지 (Single VM)

```
                    ┌──────────────────────────┐
                    │        Vercel            │
                    │  Next.js (everbit.kr)    │
                    └───────────┬──────────────┘
                                │ HTTPS (fetch)
                                ▼
                    ┌──────────────────────────┐
                    │     VM Public IP         │
                    │  api.everbit.kr (A/AAAA) │
                    └───────────┬──────────────┘
                                │ 80/443
                                ▼
┌────────────────────────────────────────────────────────────────┐
│                        Single VM (Docker)                        │
│                                                                │
│  [Public]                                                      │
│   - Nginx (TLS 종료, Reverse Proxy, Rate/ACL)                  │
│        │                                                        │
│        ├── /api → Spring Boot                                   │
│        └── /actuator → (제한된) Spring Actuator                 │
│                                                                │
│  [Private Docker Network]                                      │
│   - Spring Boot App (Java 21, Boot 4.0.2)                      │
│   - PostgreSQL (primary DB)                                    │
│   - Redis (cache/lock/rate-limit state)                        │
│   - Kafka (KRaft 단일 브로커)                                  │
│   - Prometheus (scrape)                                        │
│   - Grafana (dashboard)                                        │
│   - Jenkins (CI/CD, 선택: 외부 노출 최소화)                    │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 4. 런타임 컴포넌트 책임

### 4.1 Nginx (Edge)
- TLS 종료(Let's Encrypt)
- `api.everbit.kr` 리버스 프록시
- 정적 리소스 캐시(선택)
- WAF 수준은 아니지만 최소 보안 정책 적용
  - 허용 메서드 제한
  - 특정 경로(예: `/actuator`) IP allowlist
  - 기본 레이트 리밋(봇/스캔 방어)

### 4.2 Spring Boot (Core)
- 인증: Kakao OAuth2 → 내부 JWT 발급
- 업비트 키 관리: 등록/검증/폐기, DB 암호화 저장
- 트레이딩 엔진: 전략 실행, 리스크 게이트, 주문 파이프라인
- 백테스트: 멀티 마켓/멀티 타임프레임 실행 (Batch/Kafka 기반)
- 대시보드 API: 실행 상태/최근 주문/잔고/손익

### 4.3 PostgreSQL (DB)
- v2 기본 DB
- 트레이딩/백테스트의 소스 오브 트루스(Source of Truth)
- 주문/체결/포지션/전략설정/키관리(암호문)/아웃박스(outbox) 저장

### 4.4 Redis
- 캐시: 잔고/마켓 메타 등 고빈도 조회 캐시
- 분산락(단일 VM이라도 모듈화 목적)
- Upbit RateLimit 상태(응답 헤더 기반) 저장
- (선택) Refresh Token 저장/세션화

### 4.5 Kafka
- 비동기 파이프라인:
  - 주문 실행(CreateOrder) 큐
  - 백테스트 Job 큐
  - 이벤트(주문/포지션/알림) 전달
- 단일 브로커(KRaft)로 시작, 복제/고가용성은 v2 범위 밖
- 설계 원칙: **시장 데이터(ticker/체결)는 Kafka로 흘리지 않는다.**
  - 시장 데이터는 WebSocket로 수신 → 내부 처리
  - Kafka에는 “의사결정 결과(주문/상태 변화)”만 남긴다.

### 4.6 Prometheus / Grafana
- Spring Actuator `prometheus` 엔드포인트를 스크랩하여 메트릭 수집
- 대시보드 최소 세트:
  - 주문 성공/실패, 429 빈도, 전략 실행 지연, WebSocket 연결 상태
  - 백테스트 소요시간, Job 큐 적체량

### 4.7 Jenkins
- CI: 테스트/빌드/도커 이미지 생성
- CD: VM에 배포(SSH + docker compose pull/up)
- 비용/리소스가 빡세면, Jenkins Master는 VM에 두고 Agent는 로컬(Mac)이나 별도 무료 러너로 분리하는 옵션을 허용한다(ADR로 관리).

---

## 5. 주요 데이터 플로우

### 5.1 로그인
1. Client → Kakao OAuth2 로그인
2. Server → Kakao 사용자 확인
3. Server → JWT 발급(Access/Refresh)
4. 이후 API는 JWT 기반 인증

### 5.2 업비트 키 등록/검증
1. Client → 업비트 Access/Secret 입력
2. Server → 검증 API 호출(잔고 조회 등)
3. 성공 시: DB에 암호화 저장(Secret은 절대 평문 저장 금지)

### 5.3 자동매매 (전략 1개 + 여러 마켓)
1. Market Data(WebSocket) 수신
2. Candle close 단위로 전략 평가(EXTREME_FLIP)
3. 리스크 엔진 통과 시 OrderIntent 생성
4. OrderIntent를 DB에 저장 + Outbox 이벤트 생성
5. Outbox Relay가 Kafka로 `CreateOrder` 커맨드 발행
6. Order Executor가 Upbit 주문 생성 호출
7. 결과(성공/실패/체결)는 DB 업데이트 + Kafka 이벤트 발행
8. 대시보드 API는 DB를 조회하여 최신 상태 제공

> 상세 스펙: `docs/architecture/order-pipeline.md`

### 5.4 백테스트
1. Client → 백테스트 요청
2. Server → BacktestJob 저장 + Kafka 커맨드 발행
3. Backtest Worker → 데이터 로딩/시뮬레이션
4. 결과 저장 + 지표(CAGR/MDD/승률/수익 팩터) 산출
5. Client → 결과 조회

---

## 6. 네트워크 / 포트 정책

### 6.1 공개 포트(외부 노출)
- 80/tcp : HTTP (Let's Encrypt / redirect to 443)
- 443/tcp: HTTPS (API)
- (선택) 22/tcp : SSH (IP 제한 + 키 인증 필수)
- (선택) Jenkins/Grafana 등 Admin Surface는 **기본 비공개** (표준: SSH 터널). 예외적으로 공개가 필요하면 allowlist+인증 적용. (`docs/operations/admin-surface-policy.md`)

### 6.2 내부 포트(Docker 네트워크)
- Spring: 8080
- Postgres: 5432
- Redis: 6379
- Kafka: 9092
- Prometheus: 9090
- Grafana: 3000
- Jenkins: 8081(예시)

---

## 7. 보안 경계

- 시크릿 주입:
  - 로컬: `.env.local`
  - 운영: VM 환경변수(또는 `/etc/everbit/everbit.env` 같은 root-only 파일)
- Git/Ai 인덱싱 제외:
  - `.gitignore`, `.cursorignore`에 시크릿/로그/데이터/산출물 포함
- 업비트 키:
  - DB에는 **암호문만 저장**
  - 마스터키는 운영 환경변수로만 제공
- Actuator:
  - Prometheus는 **내부 도커 네트워크에서 스크랩**한다. 외부(인터넷)에는 `/actuator/**`를 노출하지 않는다.
- 방화벽:
  - inbound 최소화, outbound는 Upbit/Kakao 등 필요한 도메인만 허용(가능하면)

추가 정책 문서:
- `docs/security/exposure-policy.md`
- `docs/operations/admin-surface-policy.md`

---

## 8. 운영 제약 및 확장

- 단일 VM이므로 장애 시 전체 다운. v2에서는 HA를 목표로 하지 않는다.
- Kafka/Postgres를 같은 VM에 둔다 → 디스크/IO가 병목이 될 수 있다.
- 운영 중 부하가 커지면 아래 순서로 분리한다(추후 ADR):
  1) Jenkins 분리
  2) Grafana/Prometheus 분리(또는 관리형)
  3) Kafka 분리(또는 관리형/Upstash)
  4) DB 분리

---

## 9. 백업/복구(최소)

- Postgres: 하루 1회 dump + 7~14일 보관
- 중요 테이블(주문/키관리/전략설정/백테스트 결과)은 우선순위 높게
- VM 볼륨 스냅샷(클라우드 지원 시) 병행 가능

---

## 10. 레퍼런스

- Upbit Rate Limit 및 Remaining-Req 헤더 활용: https://docs.upbit.com/kr/reference/rate-limits
- Upbit 주문 생성 Rate Limit(주문 생성 그룹 초당 8회): https://docs.upbit.com/kr/reference/new-order
- Upbit WebSocket(private 포함): https://docs.upbit.com/kr/reference/websocket-guide
- Spring Boot Actuator Prometheus endpoint: https://docs.spring.io/spring-boot/reference/actuator/metrics.html
