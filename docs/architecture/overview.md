# Everbit v2 아키텍처 개요 (단일 VM 올인원)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

v2는 1인 전용(싱글 테넌트) 자동매매 시스템이며, 초기 운영 비용을 최소화하기 위해 **단일 VM(OCI Free Tier 우선) 올인원** 배포를 기본으로 한다.

v2 MVP에서는 Kafka를 **필수에서 제외**하고, PostgreSQL `outbox_event` 기반 Outbox/Queue로 비동기 파이프라인을 구성한다.

---

## 1. 시스템 컨텍스트

- **Client**: Next.js (Vercel) — `everbit.kr`
- **API**: Spring Boot 서버 (단일 VM) — `api.everbit.kr`
- **외부 연동**
  - Kakao OAuth2 (로그인 단일 수단)
  - Upbit Open API (주문/잔고/조회)
  - Upbit WebSocket (시세 + private: myOrder/myAsset)

---

## 2. 배포 토폴로지(단일 VM)

```
                    ┌──────────────────────────┐
                    │        Vercel            │
                    │  Next.js (everbit.kr)    │
                    └───────────┬──────────────┘
                                │ HTTPS
                                ▼
                    ┌──────────────────────────┐
                    │     VM Public IP         │
                    │  api.everbit.kr (DNS)    │
                    └───────────┬──────────────┘
                                │ 80/443
                                ▼
┌────────────────────────────────────────────────────────────────┐
│                        Single VM (Docker)                       │
│                                                                │
│  [Public]                                                      │
│   - Nginx (TLS 종료, Reverse Proxy, ACL/RateLimit)             │
│        │                                                        │
│        ├── /api → Spring Boot                                   │
│        └── (관리/actuator는 외부 차단)                          │
│                                                                │
│  [Private Docker Network]                                      │
│   - Spring Boot App (Java 21, Boot 4.x)                        │
│       - API + Background Workers(Outbox Polling)               │
│   - PostgreSQL (SoT + Outbox/Queue)                            │
│   - Redis (cache/locks/session/rate-state)                     │
│   - Prometheus (scrape)                                        │
│   - Grafana (dashboard)                                        │
│   - Jenkins (선택, 기본 비공개)                                │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## 3. 런타임 컴포넌트 책임(요약)

### 3.1 Nginx(Edge)
- TLS 종료(LE)
- `api.everbit.kr` 리버스 프록시
- `/actuator/**` 외부 차단(내부 스크랩 전용)
- 기본 레이트 리밋/ACL(스캔/봇 방어 최소 수준)

### 3.2 Spring Boot(Core)
- 인증: Kakao OAuth2 → JWT(Access/Refresh)
- Upbit 키 관리: 등록/검증/폐기 + 암호화 저장
- 트레이딩: 전략 평가 → 리스크 게이트 → 주문 파이프라인
- 백테스트: 멀티 마켓/타임프레임 실행(배치/워커)
- 푸시 알림: 주문 접수(ACK) 시 Web Push 발송(구독 관리 포함)
- 대시보드 API: 실행 상태/최근 주문/잔고/손익

### 3.3 PostgreSQL(SoT)
- 주문/체결/포지션/설정/키 암호문/Outbox/백테스트 결과 저장

### 3.4 Redis
- Upbit 레이트 상태(Remaining-Req 등), 락/세션(refresh_jti) 저장
- (선택) 대시보드 read-model 캐시

### 3.5 Event Bus / Queue (PostgreSQL Outbox/Queue)
- 비동기 파이프라인/버퍼를 PostgreSQL `outbox_event`로 구현한다.
- 대표 스트림:
  - `everbit.trade.command`: 주문 실행 커맨드(CreateOrderAttempt)
  - `everbit.trade.event`: 주문/체결/포지션 이벤트(알림 트리거)
  - `everbit.backtest.command`: 백테스트 job 커맨드
- 워커는 `FOR UPDATE SKIP LOCKED`로 claim 후 처리한다.

상세: `docs/architecture/event-bus.md`

### 3.6 Prometheus/Grafana
- 메트릭 스크랩/대시보드 제공(기본 비공개, SSH 터널 접근)

### 3.7 Jenkins(선택)
- CI/CD. 기본 비공개(SSH 터널)이며, 필요 시 별도 분리 고려.

---

## 4. 주요 플로우

### 4.1 로그인
1) Client → Kakao OAuth2  
2) API → OWNER 검증(싱글 테넌트)  
3) API → Access(Bearer) + Refresh(HttpOnly cookie) 발급

### 4.2 자동매매(전략 1개 + 여러 마켓)
1) Market data(WebSocket) 수신  
2) candle close 단위로 전략 평가(EXTREME_FLIP)  
3) 리스크 게이트 통과 시 Signal/OrderIntent 저장  
4) **EventBus(outbox_event)** 로 `CreateOrderAttempt` 커맨드 발행(트랜잭션 내 INSERT)  
5) **Order Executor Worker** 가 커맨드를 claim → Upbit 주문 생성(Attempt 단위)  
6) 성공(ACK): Order 저장 + `OrderAccepted` 이벤트 발행(outbox_event)  
7) 이벤트 처리:
   - 대시보드 갱신(최소)
   - **푸시 알림 발송(OrderAccepted 시)**

상세: `docs/architecture/order-pipeline.md`

### 4.3 백테스트
1) Client → 요청  
2) Job 저장 + `BacktestRun` 커맨드 발행(outbox_event)  
3) Worker 실행 → 결과 저장/지표 산출  
4) Client → 결과 조회

---

## 5. 네트워크/포트 정책

외부 노출(기본):
- 80/tcp (LE + redirect)
- 443/tcp (API)
- 22/tcp (SSH, 본인 IP /32 제한, 키 인증)

절대 외부 노출 금지:
- Postgres(5432), Redis(6379), Prometheus(9090), Grafana(3000), Jenkins 등

---

## 6. 운영 제약/확장

- 단일 VM이므로 장애 시 전체 다운(v2에서 HA 목표 아님)
- 부하 증가 시 분리 우선순위(권장):
  1) Jenkins 분리
  2) Grafana/Prometheus 분리
  3) Backtest/Worker 프로세스 분리(동일 이미지, worker 모드)
  4) DB 분리
  5) (P1+) Kafka 도입: `EventBus` 구현체 교체(ADR-0009)

---

## 7. 참고
- Upbit Rate Limit: https://docs.upbit.com/kr/reference/rate-limits
- Upbit 주문 생성/identifier: https://docs.upbit.com/kr/reference/new-order
- Upbit WebSocket: https://docs.upbit.com/kr/reference/websocket-guide
