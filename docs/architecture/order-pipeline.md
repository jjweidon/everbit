# 주문 파이프라인 상세 스펙 (멱등/재시도/레이트리밋)

## 1. 목적

실거래 시스템에서 가장 위험한 문제는 아래 3가지다.

1) **중복 주문**
2) **레이트리밋 초과로 인한 연쇄 장애**
3) **재기동/네트워크 장애 시 정합성 붕괴(주문 상태/잔고 불일치)**

이 문서는 위 문제를 피하기 위한 **주문 파이프라인의 규칙**을 고정한다.

---

## 2. 용어

- **Signal**: 전략이 생성한 매수/매도 의사결정(캔들 close 단위)
- **OrderIntent**: “주문을 내고 싶다”는 내부 의도(아직 Upbit 호출 전)
- **Order**: Upbit에 제출된 주문(Upbit UUID가 존재)
- **Fill/Trade**: 체결 이벤트(부분 체결 포함)
- **Identifier**: Upbit 주문 생성 시 전달하는 “클라이언트 주문 식별자”
- **Kill Switch**: 계정/전략 단위 주문 차단 스위치

---

## 3. 파이프라인 단계

### 3.1 시장 데이터 수신
- Public WebSocket:
  - Candle(OHLCV), Ticker, Trade 중 전략에 필요한 최소 데이터만 구독
- 원칙:
  - WebSocket 메시지를 Kafka로 전송하지 않는다(고빈도).

### 3.2 Signal 생성 (Candle close 기준)
- 입력: (market, timeframe, candle_close_time, candle)
- 출력: Signal(side, strength, params)
- Signal은 DB에 저장한다.

### 3.3 리스크 게이트 (MVP 포함)
리스크 규칙(손절/익절/트레일링 + 주문 금액 최소/최대 + Kill Switch)을 **주문 생성 직전** 강제 적용한다.

- Account Kill Switch OFF → 주문 생성 금지
- Strategy Kill Switch OFF → 해당 전략 주문 금지
- 주문 금액(min/max) 위반 → 주문 금지
- 손절/익절/트레일링 조건 충족 → 주문 생성(또는 청산 주문 생성)

### 3.4 OrderIntent 생성 및 저장
- OrderIntent는 “실행 가능한 주문”으로 변환된 결과물이다.
- DB 저장 후 Outbox 메시지를 생성한다.
- Kafka로 `CreateOrder` 커맨드가 발행된다.

### 3.5 Order Executor (Upbit 호출)
- `everbit.trade.command`를 소비
- Upbit 주문 생성 호출
- 결과를 DB에 기록하고 이벤트를 발행한다.

### 3.6 체결 처리
- 우선순위 1: Upbit **Private WebSocket myOrder**로 체결/상태 변화를 수신한다.
- 우선순위 2(보조): REST API로 주문 상태를 폴링/동기화한다(낮은 빈도).

체결 수신 → Fill 저장 → Position/PnL 업데이트 → 이벤트 발행

---

## 4. 멱등성(Idempotency) 규칙

### 4.1 Signal 멱등성
Signal의 중복 생성 자체를 막는다.

- `signal_id = hash(strategyKey + market + timeframe + candle_close_time + side)`
- DB 유니크 인덱스: `UNIQUE(signal_id)`

전략 엔진이 재기동되더라도 같은 캔들 close에 대해 Signal이 중복 생성되지 않는다.

### 4.2 OrderIntent 멱등성
- `order_intent_id`는 DB PK
- `UNIQUE(signal_id, intent_type)` 형태로 “같은 신호에 대해 같은 타입 주문이 중복 생성되지 않게” 방지

예:
- intent_type: `ENTRY`, `EXIT_STOPLOSS`, `EXIT_TAKEPROFIT`, `EXIT_TRAILING`

### 4.3 Upbit 주문 생성 멱등성(Identifier 기반)
Upbit 주문 생성은 `identifier`를 지원하며, identifier는 계정 내에서 유일해야 하고 재사용할 수 없다.

따라서 Everbit는 identifier를 다음 규칙으로 생성한다.

- `identifier = {strategyKey}:{market}:{timeframe}:{candleClose}:{intentType}:{signalHashShort}`
- 제한이 불명확하므로(문서에서 길이 명시가 항상 노출되진 않음) **최대 64자**를 기준으로 설계한다.
  - 초과 시: strategyKey/market/timeframe 축약 + hash 사용

DB 유니크 인덱스:
- `UNIQUE(identifier)` (OrderIntent 레벨)

실행 규칙:
1) CreateOrder 호출 성공 → Upbit UUID 확보 → Order 레코드 생성
2) CreateOrder 호출이 timeout/네트워크 에러 → 즉시 재시도하지 말고 `Get Order by Identifier`로 조회 시도
3) 조회 결과가 존재하면 → 이미 생성된 주문으로 간주하고 UUID를 동기화
4) 조회 결과가 없으면 → 동일 identifier로 CreateOrder 재시도(최대 N회)

> 참고: Upbit 글로벌 문서에서 “UUID 또는 Identifier로 주문 조회”가 가능하다고 명시한다.

---

## 5. 레이트리밋(Throttling) 규칙

Upbit는 REST API에 대해 Rate Limit 그룹과 잔여 요청 수를 `Remaining-Req` 헤더로 제공한다.
`group`, `sec`(초 단위 잔여 요청 수)를 기준으로 호출을 제어한다.

또한 주문 생성 API는 “주문 생성 그룹”에서 **초당 최대 8회** 제한이 명시되어 있다.

### 5.1 구현 표준
- 모든 Upbit REST 호출은 공통 모듈 `UpbitHttpClient`를 통해서만 수행한다.
- `UpbitHttpClient`는 다음을 포함한다.
  - 응답 헤더에서 Remaining-Req 파싱 → Redis/메모리에 반영
  - 그룹별 Token Bucket/Leaky Bucket 형태로 전송 전 스로틀링
  - 429 발생 시 즉시 해당 그룹 호출 중단 + 백오프(Upbit Best Practice 준수)

### 5.2 429 처리 정책
- 429 수신 시:
  - 즉시 해당 그룹 호출 중단
  - `sec`가 0이면 다음 초로 넘어갈 때까지 대기
  - 재시도는 지수 백오프 + jitter
- 429가 연속적으로 발생하면:
  - “전략 실행을 계속”하되, 주문 실행은 큐에 쌓고 executor에서 천천히 처리
  - (옵션) 일정 횟수 초과 시 알림 + 수동 Kill Switch 유도

---

## 6. 재시도(Retry) 정책

### 6.1 재시도 대상
- 네트워크 오류(타임아웃, 연결 끊김)
- 5xx
- 429(레이트리밋) → 반드시 백오프 후 재시도

### 6.2 재시도 금지
- 파라미터 오류(400)
- 권한/스코프 오류(out_of_scope 등)
- 잔고 부족/최소 주문 금액 위반(비즈니스 오류)
- Kill Switch OFF 상태

### 6.3 최대 시도 횟수(초기)
- CreateOrder: 최대 5회
- GetOrderByIdentifier: 최대 5회(짧은 간격)
- CancelOrder: 최대 3회

> 재시도는 “중복 주문 방지” 규칙(4장)을 항상 선행한다.

---

## 7. 정합성(Reconciliation) 루프

WebSocket 기반이더라도 정합성은 깨질 수 있다(끊김/유실/서버 재기동).

### 7.1 동기화 트리거
- 서버 부팅 시
- WebSocket 재연결 시
- 주기적(예: 1~5분) 점검 스케줄

### 7.2 동기화 범위(최소)
- 열린 주문(open orders) 목록 동기화
- 잔고(account) 동기화
- 미체결 주문의 상태 업데이트

> 이 루프는 호출량이 커질 수 있으므로 RateLimit을 엄격히 적용한다.

---

## 8. Kill Switch 동작 규칙

### 8.1 계정 Kill Switch
- OFF 시:
  - 신규 Signal → OrderIntent 생성 금지(또는 생성하되 실행 금지)
  - Kafka `CreateOrder` 발행 금지
  - Order Executor는 소비하더라도 즉시 `SKIPPED` 처리

### 8.2 전략 Kill Switch
- OFF 시:
  - 해당 전략Key의 커맨드/이벤트는 실행 금지
  - “설정된 전략으로만 매매 실행” 정책 유지

> 열린 주문의 자동 취소는 별도 기능으로 분리한다(취소 자체도 API 호출/리스크).

---

## 9. 구현 체크리스트 (Done 기준)

- [ ] Signal/Intent/Order 테이블 유니크 제약 설정
- [ ] Outbox 패턴 구현 (DB→Kafka 신뢰성)
- [ ] UpbitHttpClient 단일 진입점 + Remaining-Req 파싱
- [ ] 429 처리 정책 구현(즉시 중단 + 백오프)
- [ ] Private WebSocket(myOrder/myAsset) 기반 체결/자산 반영
- [ ] 재기동 시 Reconciliation 루프 구현
- [ ] Kill Switch가 파이프라인 모든 단계에서 강제되는지 테스트

---

## 10. 레퍼런스

- Remaining-Req 헤더 포맷 및 sec 의미: https://docs.upbit.com/kr/reference/rate-limits
- 429 처리 Best Practice: https://docs.upbit.com/kr/docs/rest-api-best-practice
- 주문 생성 Rate Limit 및 identifier 재사용 금지: https://docs.upbit.com/kr/reference/new-order
- 주문 조회는 UUID 또는 Identifier로 가능(글로벌 문서): https://global-docs.upbit.com/reference/get-order
- WebSocket Endpoint(private 포함): https://docs.upbit.com/kr/reference/websocket-guide
