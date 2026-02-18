# Everbit v2 MVP 로드맵 및 완료 체크리스트

Status: **Active (Tracking)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

이 문서는 everbit v2 MVP를 **“개발 착수 → 기능 완성 → 운영 준비 → 실거래 ON”**까지 단계별로 추적하기 위한 **단일 체크리스트(SoT)** 이다.  
작업은 이 문서의 체크박스를 기준으로 진행하며, 완료 여부는 각 항목의 **Definition of Done(DoD)** 로 판단한다.

---

## 사용 규칙

- 체크박스는 아래 규칙으로 운영한다.
  - `- [ ]` 미완료
  - `- [x]` 완료(DoD 충족)
- “문서만 작성”은 완료가 아니다. **코드/테스트/운영 증거물**까지 갖추면 체크한다.
- P0(필수): v2 MVP에서 실거래 안정성을 좌우하는 항목. P0가 남아 있으면 **실거래 ON 금지**.
- P1+(선택): MVP 이후로 미뤄도 되는 항목.

---

## 게이트(Gates)

### Gate A — 개발 착수(코드 들어가기) 최소 조건
- 목표: DB/Outbox/주문 파이프라인을 **스텁(모의 Upbit)** 로라도 end-to-end로 관통시킬 수 있는 기반 확보
- Gate A 통과 조건: `Phase 0` + `Phase 1`의 P0 항목 완료

### Gate B — 통합 테스트(스테이징) 배포 가능
- 목표: 실 Upbit 연동을 붙이되, “돈이 나가지 않는 모드(드라이런/페이퍼)”에서 안정성 검증
- Gate B 통과 조건: `Phase 2~6`의 P0 항목 완료 + 모니터링/알림 최소 구축

### Gate C — 실거래 ON(프로덕션 자동 주문) 가능
- 목표: 장애/레이트리밋/중복주문/스톨에 대한 운영 대응까지 준비된 상태
- Gate C 통과 조건: 아래 “실거래 ON 체크리스트” P0 전부 완료

---

## Phase 0. 결정 고정(문서/규격) — P0

> 이 단계는 “구현자 해석 여지”를 제거하는 작업이다. 코드 작성 전에 고정하면 비용이 가장 낮다.

- [ ] **Spring Boot / Java / Hibernate 버전 고정(단일 기준)**
  - DoD:
    - 사용할 버전(예: Boot 3.3.x, Java 21 등) 문서에 1곳으로 고정
    - ADR 또는 `operations/environments.md`에 명시
- [ ] **Outbox 스테일 락 복구 전략 1안으로 고정**
  - 선택지(둘 중 1개만 채택):
    1) Claim 대상에 `PROCESSING AND locked_until < now()` 포함(재-claim)
    2) Sweeper가 주기적으로 `PROCESSING(expired)` → `PENDING`으로 복구
  - DoD:
    - `architecture/event-bus.md`에 SQL/동작 규격을 1개로 고정
    - 테스트(통합 또는 단위)로 “워커 죽음 후 복구” 재현 가능
- [ ] **429(THROTTLED) 재시도 표준 고정**
  - DoD:
    - “429 수신 → 기존 attempt 종결 → 새 attempt(attempt_no+1, 새 identifier) 생성 → outbox 재발행”을 문서로 고정
    - WireMock/Mock 기반 회귀 테스트 케이스 추가 계획까지 포함
- [ ] **Attempt 요청 파라미터 스냅샷 정책 고정**
  - 선택지:
    1) `order_intent`에서 파생 규칙을 문서로 고정(완전 결정적이어야 함)
    2) `order_attempt.request_json(jsonb)` 또는 outbox payload에 snapshot을 고정 저장
  - DoD:
    - 선택한 방식이 문서/스키마/코드 규칙에 반영됨
- [ ] **백테스트 캔들 데이터 전략 결정**
  - 선택지:
    1) DB에 candle 캐시 테이블을 둔다(권장: (market,timeframe,candle_time) UNIQUE)
    2) P0에서는 저장 없이 외부에서 매번 로딩(레이트리밋/재현성 리스크 수용)
  - DoD:
    - `requirements/functional.md`의 백테스트 범위와 정합
    - `testing/performance-plan.md`과 충돌 없음
- [ ] **API 계약 최소치 고정(엔드포인트/DTO/에러)**
  - 범위(최소):
    - 로그인/세션, UpbitKey 등록/검증, KillSwitch, Strategy/Market config, Backtest job, Push subscription
  - DoD:
    - `integrations` 또는 `api` 문서에 스키마가 고정되고, FE/BE 병렬 개발이 가능함

---

## Phase 1. 프로젝트 기반(Repo/DB/CI) — P0

- [ ] **Repo/모듈 구조 확정**
  - DoD:
    - 패키지 구조가 `architecture/components.md`와 일치
    - “domain/service/repository/controller” 경계가 문서로 고정
- [ ] **TDD 개발 루프(RED→GREEN→REFACTOR) 적용 + 테스트 스캐폴딩 구축**
  - DoD:
    - `docs/testing/tdd.md`가 팀의 단일 기준으로 사용되고, PR 리뷰에서 체크된다
    - Unit/Integration(Testcontainers)/WireMock 기본 템플릿이 프로젝트에 존재한다
    - P0 영역(주문/Outbox/레이트리밋) 대표 회귀 테스트 1세트가 CI에서 항상 실행된다
    - `docs/testing/strategy.md`의 CI 게이트(P0)가 실제 파이프라인에 반영된다

- [ ] **DB 마이그레이션 체계(Flyway 또는 Liquibase) 도입**
  - DoD:
    - `db/schema-v2-mvp.sql`을 마이그레이션 파일로 이행
    - 로컬에서 `clean -> migrate -> app boot` 재현 가능
- [ ] **로컬 개발 환경(docker-compose) 표준화**
  - DoD:
    - Postgres, (선택) Redis, (선택) Grafana/Prometheus 구성이 문서화
    - 신규 개발자가 30분 내 기동 가능
- [ ] **관측(Logging/Metrics) 최소 프레임 구축**
  - DoD:
    - correlation id(요청/이벤트) 로그 표준 적용
    - Outbox backlog/처리량/실패율 메트릭 최소 노출

---

## Phase 2. 인증/계정(Owner) — P0

- [ ] **OAuth2 로그인 + 세션 정책 구현**
  - DoD:
    - ADR-0007(세션)과 구현이 일치
    - `app_user` 생성/조회가 멱등적으로 동작(중복 생성 없음)
- [ ] **Admin Surface 접근 정책 적용**
  - DoD:
    - `operations/admin-surface-policy.md` 준수
    - Grafana/Jenkins 등 보호 확인

---

## Phase 3. Upbit 키 관리(암호화/로테이션) — P0

- [ ] **UpbitKey 저장(암호화)**
  - DoD:
    - 평문 저장 금지(로그/DB 모두)
    - AES-GCM + key_version 로테이션 규칙 반영(ADR-0005)
- [ ] **키 검증/헬스체크 API**
  - DoD:
    - 유효한 키/무효한 키 케이스 분리
    - 실패 시 사용자에게 안전한 오류 메시지/코드 제공

---

## Phase 4. Event Bus(Outbox/Queue) — P0

- [ ] **Outbox 발행 라이브러리(트랜잭션 내 INSERT)**
  - DoD:
    - 도메인 변경(SoT)과 outbox INSERT가 동일 트랜잭션으로 묶임
- [ ] **워커(consumer) claim/처리/ack 구현**
  - DoD:
    - `FOR UPDATE SKIP LOCKED` 기반 claim
    - 처리 결과에 따른 상태 전이(PENDING/PROCESSING/DONE/DEAD)
- [ ] **재시도/백오프 구현**
  - DoD:
    - attempt_count / next_retry_at 기반 스케줄링
    - max_attempts 초과 시 DEAD 전환 + 운영 대응 절차
- [ ] **스테일 락 복구 구현(Phase 0에서 결정한 방식)**
  - DoD:
    - 워커 강제 종료 후에도 이벤트가 최종 DONE/DEAD로 수렴

---

## Phase 5. 주문 파이프라인(모의 Upbit) — P0

> 실 Upbit 연동 전에 “상태 머신/멱등/재시도”를 먼저 검증한다.

- [ ] **Signal → OrderIntent 생성(멱등)**
  - DoD:
    - `signal` UNIQUE 키로 중복 방지
    - `order_intent` UNIQUE(signal_id,intent_type)로 중복 방지
- [ ] **OrderAttempt 생성/identifier 정책 준수**
  - DoD:
    - attempt_no 단조 증가
    - identifier는 절대 재사용 금지(UNIQUE)
- [ ] **모의 Upbit(WireMock)로 상태별 회귀 테스트**
  - 필수 케이스:
    - 정상 ACKED
    - 429 THROTTLED → 새 attempt 생성 + 예약 재시도
    - 418 제한 → SUSPENDED/킬스위치 전환(정책에 따라)
    - timeout/unknown → UNKNOWN 상태 수렴 + reconcile 트리거
  - DoD:
    - 위 케이스가 자동 테스트로 고정(재현 가능)
- [ ] **KillSwitch 반영(주문 차단)**
  - DoD:
    - account_enabled=false면 신규 intent/attempt 생성이 차단되거나 즉시 취소로 수렴
    - 운영자가 즉시 멈출 수 있음

---

## Phase 6. Upbit 실연동(REST + WS) — P0

- [ ] **REST 주문/조회 클라이언트**
  - DoD:
    - 레이트리밋 정책 준수(429 대응 포함)
    - identifier/upbit_uuid 매핑이 문서와 일치
- [ ] **Private WebSocket(myOrder) 소비**
  - DoD:
    - 체결/상태 업데이트가 `fill.trade_uuid` 멱등으로 적재됨
    - WS 끊김/재연결 정책이 문서화/구현됨
- [ ] **Reconcile(재동기화) 잡**
  - DoD:
    - UNKNOWN/PROCESSING 잔존을 주기적으로 수렴(DONE/DEAD/ACKED 등)
    - 장애/재시작 후에도 상태가 안정적으로 회복

---

## Phase 7. 알림(Web Push) — P0

- [ ] **Push subscription 등록/삭제/비활성화**
  - DoD:
    - `push_subscription` UNIQUE(owner_id,endpoint) 준수
- [ ] **주요 이벤트 알림 발행**
  - DoD:
    - outbox 기반으로 “주문 접수/체결/에러/킬스위치” 알림이 전송됨
    - 중복 알림이 DB 멱등 키로 방지됨(선택: notification_log)

---

## Phase 8. 백테스트 — P0/P1(결정에 따름)

- [ ] **Backtest job 큐잉/실행**
  - DoD:
    - backtest_job 상태(QUEUED/RUNNING/DONE/FAILED) 수렴
    - 워커 장애 시 재시도/복구 가능
- [ ] **결과 저장(backtest_result)**
  - DoD:
    - UNIQUE(job_id) 또는 PK(job_id)로 1회 결과 보장
- [ ] **캔들 데이터 전략 구현(Phase 0 결정에 따름)**
  - DoD:
    - 성능 계획(`testing/performance-plan.md`) 기준을 충족하거나, 충족 불가 시 제한을 문서화

---

## Phase 9. 운영 준비(관측/알림/배포/DR) — P0

- [ ] **메트릭/대시보드 최소 구축**
  - 필수:
    - Outbox backlog(개수/최대 지연)
    - 처리 실패율/DEAD 증가율
    - Upbit 429/418 카운트
    - 주문 end-to-end latency
    - WS disconnect 횟수
  - DoD:
    - Grafana 대시보드 + 주요 알림 룰이 존재
- [ ] **런북/장애 대응 시나리오 점검**
  - DoD:
    - outbox 스톨/DEAD 증가/418 발생/WS 불안정 시 대응 절차가 실행 가능
- [ ] **배포 파이프라인(스테이징/프로덕션)**
  - DoD:
    - 한 번의 커밋으로 스테이징까지 자동 배포(또는 명확한 수동 절차)
- [ ] **DR(복구) 리허설 최소 1회**
  - DoD:
    - DB 백업/복구 절차가 실제로 동작(시간/데이터 손실 범위 기록)

---

## 실거래 ON 체크리스트(P0) — Gate C

- [ ] **킬스위치가 1초 내로 주문 생성 경로를 차단한다**
- [ ] **429 대응이 “새 attempt + 예약 재시도”로 검증됐다(WireMock + 스테이징)**
- [ ] **418 발생 시 시스템이 자동으로 안전 모드(SUSPENDED/kill)로 전환한다**
- [ ] **Outbox 스테일 락 복구가 장애 시나리오에서 검증됐다**
- [ ] **UNKNOWN 상태가 reconcile로 수렴된다(수동 개입 없이도)**
- [ ] **민감정보(Upbit 키/시크릿)가 로그/응답/DB 평문으로 노출되지 않는다**
- [ ] **운영 알림(Outbox backlog/DEAD/429/418)이 실제로 울린다(테스트 발사)**
- [ ] **프로덕션 배포/롤백 절차가 검증됐다**
- [ ] **실거래 전 “페이퍼/드라이런 모드”로 최소 24시간 연속 실행 검증(권장)**

---

## 추천 운영 루틴(간단)

- 매일: Outbox backlog/DEAD/429/418/WS 상태 확인
- 매주: reconcile 결과(UNKNOWN 잔존 여부), 주문 성공률/지연 확인
- 매월: 키 로테이션/권한 점검, DR 절차 리허설(최소 분기 1회)

