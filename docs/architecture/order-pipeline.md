# 주문 파이프라인 상세 스펙 (멱등/재시도/레이트리밋)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

실거래 시스템에서 가장 위험한 문제는 아래 3가지다.

1) **중복 주문**
2) **레이트리밋 초과 → 418 차단 → 연쇄 장애**
3) **재기동/네트워크 장애 시 정합성 붕괴(주문/잔고 불일치)**

이 문서는 위 문제를 방지하기 위한 “강제 규칙”을 고정한다.

---

## 1. 용어

- **Signal**: 전략이 생성한 매수/매도 의사결정(확정 캔들 close 기준)
- **OrderIntent**: 주문 의도(Upbit 호출 전 단계)
- **OrderAttempt**: Upbit 주문 생성 호출 1회(Attempt마다 identifier는 신규 생성)
- **UpbitOrder**: Upbit UUID가 존재하는 주문 실체
- **Fill**: 체결 이벤트(부분 체결 포함)
- **Kill Switch**: 계정/전략 단위 주문 차단 스위치
- **SUSPENDED**: 안전 중단 상태(불확실성/차단/정책 위반)

---

## 2. 핵심 원칙(강제)

1) **SoT는 PostgreSQL**
- 멱등/상태는 DB 유니크 제약 + 상태머신으로 보장한다.

2) **Kafka는 at-least-once**
- 중복 수신이 정상이다. 소비자는 idempotent하게 동작해야 한다.

3) **Upbit identifier는 멱등키가 아니다**
- identifier는 재사용 금지이며, 조회/상관관계(correlation) 용도만 허용한다.
- 멱등은 DB(OrderIntent/Attempt)로 해결한다.

4) **UNKNOWN은 자동 재주문으로 해결하지 않는다**
- 중복 주문 위험이 있으므로 UNKNOWN은 reconcile로 회복하고, 실패 시 시장 단위 SUSPENDED로 안전 중단한다.

---

## 3. 파이프라인 단계

### 3.1 시장 데이터 수신
- WebSocket에서 필요한 최소 데이터만 수신한다.
- 시장 데이터 원본은 Kafka로 흘리지 않는다.

### 3.2 Signal 생성(확정 캔들 close)
- 입력: (market, timeframe, candle_close_time, candle)
- 출력: Signal(side, strength, reason_code, snapshot)
- Signal은 DB에 저장한다(멱등 제약 필수).

### 3.3 리스크 게이트(필수)
주문 생성 직전에 아래를 강제한다.
- Account Kill Switch OFF → 주문 생성 금지
- Strategy Kill Switch OFF → 해당 전략 주문 금지
- 주문 금액(min/max) 위반 → 주문 금지
- 손절/익절/트레일링 조건 → 청산 intent 생성

### 3.4 OrderIntent 생성 + Outbox
- OrderIntent 저장(유니크: signal_id + intent_type)
- Outbox row 생성
- Outbox Relay가 `CreateOrderAttempt` 커맨드를 발행

### 3.5 Order Executor(Attempt 실행)
- `everbit.trade.command` 소비
- `orderAttemptId`로 DB 조회 → Upbit 호출
- 결과를 DB에 기록(Attempt 상태 전이)
- 이벤트(outbox) 발행

### 3.6 체결/정합성 처리
- 우선: Upbit private WS(myOrder/myAsset)로 체결/잔고 반영
- 보조: reconcile 루프(저빈도)로 open orders/balance 동기화

---

## 4. 멱등성 규칙(필수)

### 4.1 Signal 멱등
- 유니크 인덱스:
  - UNIQUE(owner_id, strategy_key, market, timeframe, candle_close_time, side)
- 동일 캔들/동일 side는 중복 Signal을 만들 수 없다.

### 4.2 OrderIntent 멱등
- 유니크 인덱스:
  - UNIQUE(signal_id, intent_type)
- 같은 신호로 동일한 intent(ENTRY/EXIT 등)를 중복 생성할 수 없다.

### 4.3 OrderAttempt 멱등
- 유니크 인덱스:
  - UNIQUE(order_intent_id, attempt_no)
  - UNIQUE(identifier)
- Kafka 중복 소비로 executor가 같은 attempt를 다시 실행해도, Attempt 상태를 보고 no-op 처리해야 한다.

---

## 5. Upbit 주문 생성 안전 실행(Attempt 기반)

### 5.1 Attempt 생성 규칙
- OrderIntent 1개는 여러 OrderAttempt를 가질 수 있다.
- Attempt 생성 시:
  - attempt_no = 1..N
  - identifier는 신규 생성(ULID/UUID 권장)
  - status = PREPARED
- Attempt의 Upbit 요청 파라미터(side/ord_type/price/volume/time_in_force)는 “실행 시점 스냅샷”으로 고정한다(재현성).

### 5.2 Attempt 실행 규칙(상태 전이)
1) PREPARED → SENT: Upbit CreateOrder 요청을 보냄
2) 요청 성공(2xx) + uuid 확보:
   - SENT → ACKED
   - UpbitOrder 저장/갱신
   - 이벤트: OrderAccepted (푸시 트리거)
3) 비즈니스 오류(4xx 파라미터/권한/잔고 등):
   - SENT → REJECTED
   - 자동 재시도 금지
4) 429(레이트리밋):
   - SENT → THROTTLED
   - next_retry_at 설정
   - 이후 **새 Attempt(attempt_no+1, 새 identifier)**로 재시도
5) timeout/네트워크/5xx:
   - SENT → UNKNOWN
   - reconcile(조회) 루프 수행(짧은 기간)
   - 확인 성공 시 ACKED로 승격
   - 확인 실패 시 **시장 단위 SUSPENDED**로 전환(자동매매 중단)

> 중요: UNKNOWN에서 “새 주문을 재주문”하는 자동화는 중복 주문 위험이 커서 금지한다.

---

## 6. 레이트리밋/차단 규칙(필수)

Upbit REST 응답 헤더 `Remaining-Req`를 파싱하여 group/sec 기준으로 호출을 제어한다.

### 6.1 구현 표준(강제)
- 모든 Upbit REST 호출은 `UpbitHttpClient` 단일 진입점만 사용한다.
- `UpbitHttpClient`는 다음을 수행한다.
  - Remaining-Req 파싱 → 그룹별 상태 저장(메모리/Redis)
  - 그룹별 스로틀링(Token bucket 등)
  - 429 수신 시 해당 그룹 호출 즉시 중단 + 백오프

### 6.2 429 처리(THROTTLED)
- 429 수신 시:
  - 즉시 중단 + 백오프
  - Attempt는 THROTTLED로 종료
  - 재시도는 새 Attempt로 수행

### 6.3 418 처리(BLOCKED)
- 418 수신 시:
  - 즉시 Exchange REST 호출 중단
  - 차단 해제 시각까지 재시도 금지
  - 운영 경고/알림 + 자동매매 중단(계정 Kill Switch OFF 유도 또는 자동 OFF)

---

## 7. Reconciliation(정합성 회복) 루프

### 7.1 트리거
- 서버 부팅 시
- WebSocket 재연결 시
- 주기적(예: 1~5분) 점검

### 7.2 최소 범위
- open orders 동기화
- 잔고 동기화
- DB의 “ACKED지만 미확정” 주문 상태 확인

주의:
- 호출량이 커질 수 있으므로 레이트리밋을 엄격히 적용한다.
- reconcile은 “정합성 회복”이지 “트레이딩 로직”이 아니다.

---

## 8. Kill Switch 동작(필수)

### 8.1 계정 Kill Switch OFF
- 신규 Signal → OrderIntent 생성 금지(또는 생성하되 실행 금지 중 하나로 고정)
- 표준: **Intent 생성은 허용하되, Attempt 생성/발행을 금지** (운영 분석 가능)
- executor는 커맨드를 소비하더라도 즉시 SKIPPED 처리

### 8.2 전략 Kill Switch OFF
- 해당 전략Key의 Intent/Attempt 생성을 금지

---

## 9. 푸시 알림 연계(요약)

- `OrderAccepted` 이벤트 발생 시 `notification-worker`가 WebPush 발송(FRD: FR-NOTI-001)
- 푸시는 SoT가 아니므로 실패해도 거래 흐름은 유지한다(단, 실패 구독은 정리).
- 상세: `docs/architecture/push-notifications.md`

---

## 10. Done 체크리스트(구현 게이트)

- [ ] Signal/OrderIntent/OrderAttempt 유니크 제약 설정(필수)
- [ ] Outbox 패턴 구현(DB→Kafka 신뢰성)
- [ ] UpbitHttpClient 단일 진입점 + Remaining-Req 파싱
- [ ] 429/418/UNKNOWN 상태 전이 테스트(회귀)
- [ ] Private WS(myOrder/myAsset) 기반 체결/자산 반영
- [ ] reconcile 루프 + SUSPENDED 전환
- [ ] Kill Switch가 모든 단계에서 강제되는지 E2E로 검증
