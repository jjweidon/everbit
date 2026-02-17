# Upbit 연동 스펙 (REST + WebSocket)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- Upbit 연동을 “주문 파이프라인(멱등/UNKNOWN 수렴/레이트리밋)”과 충돌 없이 구현한다.
- 429(요청 제한)/418(차단)에서 자동매매를 안전 중단(degrade/stop)한다.
- REST/WS 장애가 **중복 주문** 또는 **추격 주문**으로 이어지지 않게 한다.

비목표(v2 MVP):
- 브라우저(클라이언트)에서 Upbit 직접 호출 (금지)
- 다중 거래소/다중 계정 지원
- 고빈도 시세 원본 스트리밍을 Kafka로 전송

관련 문서(SoT):
- `docs/architecture/order-pipeline.md` (UNKNOWN 수렴/재시도/멱등)
- `docs/architecture/kafka-topics.md` (이벤트/알림 트리거)
- `docs/security/secrets.md` (키 저장/로깅 금지)

참조(공식 문서):
- 인증/권한: https://docs.upbit.com/kr/reference/auth
- REST 에러: https://docs.upbit.com/kr/reference/rest-api-guide
- Rate Limit: https://docs.upbit.com/kr/reference/rate-limits
- REST Best Practice: https://docs.upbit.com/kr/docs/rest-api-best-practice
- WebSocket 가이드: https://docs.upbit.com/kr/reference/websocket-guide
- WebSocket Best Practice: https://docs.upbit.com/kr/docs/websocket-best-practice
- 주문 생성: https://docs.upbit.com/kr/reference/new-order
- 주문 생성 테스트: https://docs.upbit.com/kr/reference/order-test

---

## 1. 강제 원칙

1) **Upbit 호출은 서버에서만 수행**
- 클라이언트는 everbit API만 호출한다.

2) **정합성(SoT)은 PostgreSQL**
- Upbit 응답/WS 이벤트는 “관측값”이며, 최종 상태는 DB 상태 머신이 결정한다.

3) **주문 생성은 절대 자동 중복 호출 금지**
- 네트워크/타임아웃/5xx 등 “생성 여부 불확실”은 재호출로 해결하지 않는다.
- Attempt는 UNKNOWN으로 기록하고 reconcile로 확정한다(상세: order-pipeline).

4) **429는 즉시 감속, 418은 즉시 중단**
- 429: 해당 그룹 호출 중단 + 백오프 후 재시도(폭주 금지)
- 418: 자동매매 즉시 중단 + 수동 복구

5) **시크릿/키/주문 파라미터 로깅 금지**
- Access/Secret Key는 절대 로그에 남기지 않는다.
- 파라미터는 최소만(마스킹) 남기고 attemptId/identifier로 상관관계 추적한다.

---

## 2. API 사용 범위 (v2 MVP)

### 2.1 REST (Quotation: 시세/캔들)
목적:
- 백테스트/시뮬레이션용 과거 데이터 적재
- WebSocket 장애 시 제한적 폴백

정책:
- 대량 적재는 백오프/배치로 수행한다.
- 시장 데이터 원본(고빈도)은 Kafka로 흘리지 않는다.

### 2.2 REST (Exchange: 거래/자산)
목적:
- 키 검증/잔고/주문 생성/조회/취소
- reconcile(부팅/재연결/UNKNOWN 확정)

정책:
- 실거래 경로에서 `/v1/orders/test`는 “사전 검증” 용도. 실패 시 즉시 주문 중단.
- 주문 조회는 uuid를 우선 사용한다(identifier는 상관관계 용도).

---

## 3. 인증(JWT) 정책

### 3.1 REST 인증
- REST 호출은 `Authorization: Bearer <JWT>` 헤더를 사용한다.
- JWT 생성 로직은 Adapter 단일 모듈로 캡슐화한다.

### 3.2 WebSocket(private) 인증
- private WS 연결은 Authorization 헤더에 Bearer JWT를 포함한다.

---

## 4. Rate Limit / Throttling 정책

### 4.1 Remaining-Req 헤더 처리(강제)
- `group`, `sec`을 파싱하여 group/sec 기준으로 호출을 제어한다.
- 파싱 실패 시 보수적 제한(예: 1 req/sec).

### 4.2 429 처리(강제)
- 429 수신 즉시 해당 group 호출 중단 + 백오프
- 주문 생성 Attempt는 `THROTTLED`로 종료하고 새 Attempt로 재시도 여부를 결정한다.

### 4.3 418 처리(강제)
- 418 수신 즉시 Upbit 호출 중단
- 계정 Kill Switch OFF + 시장 SUSPENDED 전환
- 재개는 수동

---

## 5. REST 호출 정책(Timeout/Retry)

### 5.1 Timeout (권장)
- connect: 2~3s
- read: 3~5s
- 전체 상한: 5~8s

### 5.2 Retry(강제)
- GET(조회성): 네트워크/일시적 5xx 한정, 제한적 재시도(최대 2회)
- POST `/v1/orders`(주문 생성): timeout/네트워크/5xx는 재시도 금지(UNKNOWN 수렴)

---

## 6. 주문 생성 파라미터 정책

### 6.1 공통
- `market`: `KRW-BTC`
- `side`: `bid`(매수) / `ask`(매도)
- `identifier`: Attempt별 신규 생성(ULID/UUID)

주의:
- identifier는 멱등키가 아니다.

### 6.2 ord_type 정책(Upbit 규격)
- 지정가: `limit` (volume/price)
- 시장가 매수: `price` (price)
- 시장가 매도: `market` (volume)

---

## 7. WebSocket 정책

### 7.1 Endpoint
- Quotation: `wss://api.upbit.com/websocket/v1`
- Private: `wss://api.upbit.com/websocket/v1/private`

### 7.2 연결 유지(강제)
- 30~60초 주기로 ping
- 재연결은 지수 백오프(1s→2s→5s→10s→30s cap)

### 7.3 private 구독 규칙(강제)
- `myOrder`: 주문/체결
- `myAsset`: 자산

주의:
- `myAsset`은 codes를 지원하지 않는다.

구독 메시지 예시:
```json
[
  {"ticket":"everbit-private"},
  {"type":"myOrder","codes":[]},
  {"type":"myAsset"},
  {"format":"SIMPLE"}
]
```

### 7.4 myAsset 초기 지연 대응(강제)
- private WS 연결 직후 `/v1/accounts`로 1회 스냅샷 동기화
- 이후 myAsset은 증분 업데이트

### 7.5 reconcile 트리거(권장)
- 서버 부팅
- WS 재연결
- UNKNOWN/IN_FLIGHT 장기 지속

---

## 8. 운영 체크리스트

- [ ] Upbit API Key 허용 IP에 운영 VM Public IP 등록
- [ ] 필요한 권한(자산조회/주문하기/주문조회) 포함
- [ ] 로그에 키/토큰/원문 endpoint 노출 없음
- [ ] 429 감속 동작 확인
- [ ] 418 즉시 중단 동작 확인
- [ ] private WS Authorization 연결 확인
- [ ] ping/재연결 동작 확인
