# 데이터 모델 (PostgreSQL)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

이 문서는 “정합성/멱등/상태 머신”을 DB 레벨에서 보장하기 위한 최소 스키마를 고정한다.  
구현은 JPA를 사용하더라도, **유니크 제약과 상태 전이는 이 문서가 최종 기준**이다.

---

## 1. 공통 규칙

### 1.1 키/시간
- PK는 `bigint`(sequence) 또는 UUID 중 하나로 통일한다(권장: bigint).
- 시간은 `timestamptz`(UTC)로 저장하고, 표시만 KST로 변환한다.

### 1.2 JSON 사용
- 전략 파라미터는 `jsonb`로 저장한다(스키마 버전 필수).

### 1.3 상태/이벤트
- 상태 전이는 “이전 상태 → 다음 상태”가 명시되어야 한다(임의 업데이트 금지).
- 도메인 이벤트 발행은 Outbox로 수행한다.

---

## 2. 핵심 테이블(트레이딩)

### 2.1 user (싱글 테넌트 OWNER)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| kakao_id | varchar unique | 최초 로그인 계정 고정 |
| email | varchar nullable | |
| created_at | timestamptz | |

제약:
- UNIQUE(kakao_id)

---

### 2.2 upbit_key (암호문 저장)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK(user.id) | |
| access_key_enc | bytea | AES-GCM ciphertext |
| secret_key_enc | bytea | AES-GCM ciphertext |
| key_version | int | 로테이션 용 |
| created_at | timestamptz | |
| rotated_at | timestamptz nullable | |

제약:
- UNIQUE(owner_id)  (싱글 테넌트: 1세트만 유지)

---

### 2.3 strategy_config
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| strategy_key | varchar | 예: EXTREME_FLIP |
| config_version | int | 증가 버전 |
| config_json | jsonb | 파라미터 본문 |
| updated_at | timestamptz | |

제약:
- UNIQUE(owner_id, strategy_key)
- (선택) config_version은 history 테이블로 분리 가능(P1)

---

### 2.4 market_config
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| market | varchar | KRW-BTC |
| enabled | boolean | |
| priority | int | 동시 신호 시 tie-break 보조 |
| updated_at | timestamptz | |

제약:
- UNIQUE(owner_id, market)

---

### 2.5 kill_switch
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| account_enabled | boolean | 계정 킬스위치 |
| enabled_strategies | jsonb | 허용 전략 목록(또는 별도 테이블) |
| updated_at | timestamptz | |

제약:
- UNIQUE(owner_id)

---

### 2.6 signal (멱등의 시작점)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| strategy_key | varchar | |
| market | varchar | |
| timeframe | varchar | 예: 15m |
| candle_close_time | timestamptz | 확정 캔들 종료 시각 |
| side | varchar | BUY/SELL(의사결정) |
| strength | numeric | 우선순위 점수 |
| reason_code | varchar | |
| signal_json | jsonb | 계산 근거(선택) |
| created_at | timestamptz | |

제약(필수):
- UNIQUE(owner_id, strategy_key, market, timeframe, candle_close_time, side)

---

### 2.7 order_intent (주문 의도)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| signal_id | bigint FK(signal.id) | |
| intent_type | varchar | ENTRY/EXIT_STOPLOSS/EXIT_TP/EXIT_TRAIL/EXIT_TIME |
| status | varchar | CREATED/CANCELED/COMPLETED |
| market | varchar | |
| side | varchar | bid/ask 또는 BUY/SELL(내부 표준) |
| requested_krw | numeric nullable | bid-price 주문용 |
| requested_volume | numeric nullable | ask-market 주문용 |
| reason_code | varchar | |
| created_at | timestamptz | |

제약(필수):
- UNIQUE(signal_id, intent_type)

---

### 2.8 order_attempt (Upbit 호출 1회)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| order_intent_id | bigint FK(order_intent.id) | |
| attempt_no | int | 1..N |
| identifier | varchar | Attempt별 신규(재사용 금지) |
| upbit_uuid | varchar nullable | ACK 시 확보 |
| status | varchar | PREPARED/SENT/ACKED/REJECTED/THROTTLED/UNKNOWN/SUSPENDED |
| error_code | varchar nullable | |
| error_message | text nullable | |
| next_retry_at | timestamptz nullable | 429 전용 |
| created_at | timestamptz | |
| updated_at | timestamptz | |

제약(필수):
- UNIQUE(order_intent_id, attempt_no)
- UNIQUE(identifier)

---

### 2.9 upbit_order (실체)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| upbit_uuid | varchar unique | |
| identifier | varchar nullable | 조회/상관관계 |
| market | varchar | |
| side | varchar | bid/ask |
| ord_type | varchar | limit/market/price/best |
| state | varchar | wait/done/cancel ... |
| price | numeric nullable | |
| volume | numeric nullable | |
| executed_volume | numeric | |
| created_at | timestamptz | |
| updated_at | timestamptz | |

제약:
- UNIQUE(upbit_uuid)

---

### 2.10 fill (체결)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| upbit_uuid | varchar | 주문 uuid |
| trade_time | timestamptz | |
| price | numeric | |
| volume | numeric | |
| fee | numeric nullable | |
| created_at | timestamptz | |

인덱스:
- INDEX(upbit_uuid, trade_time)

---

### 2.11 position (마켓별 포지션)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| market | varchar | |
| quantity | numeric | 보유 수량 |
| avg_price | numeric | 평균단가 |
| status | varchar | FLAT/OPEN/SUSPENDED |
| updated_at | timestamptz | |

제약:
- UNIQUE(owner_id, market)

---

### 2.12 pnl_snapshot (손익 스냅샷)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| market | varchar | |
| realized_pnl | numeric | |
| unrealized_pnl | numeric | |
| equity | numeric | |
| captured_at | timestamptz | |

인덱스:
- INDEX(owner_id, captured_at)

---

## 3. Outbox(필수)

### 3.1 outbox
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| aggregate_type | varchar | 예: ORDER_INTENT, ORDER_ATTEMPT |
| aggregate_id | bigint | |
| event_type | varchar | CreateOrder/OrderAccepted/... |
| payload_json | jsonb | |
| status | varchar | PENDING/SENT/FAILED |
| created_at | timestamptz | |
| sent_at | timestamptz nullable | |

인덱스:
- INDEX(status, created_at)

---

## 4. 푸시 알림

### 4.1 push_subscription
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| endpoint | text | subscription endpoint |
| p256dh | text | key |
| auth | text | key |
| user_agent | text nullable | |
| enabled | boolean | |
| created_at | timestamptz | |
| updated_at | timestamptz | |

제약:
- UNIQUE(owner_id, endpoint)

### 4.2 notification_log (선택, P0에서는 간단히)
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| event_id | varchar | dedupe 용 |
| type | varchar | ORDER_ACCEPTED 등 |
| payload_json | jsonb | |
| delivered_at | timestamptz nullable | |
| created_at | timestamptz | |

---

## 5. 백테스트

### 5.1 backtest_job
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| owner_id | bigint FK | |
| status | varchar | QUEUED/RUNNING/DONE/FAILED |
| request_json | jsonb | markets/timeframes/period/params |
| created_at | timestamptz | |
| updated_at | timestamptz | |

### 5.2 backtest_result
| Column | Type | Notes |
|---|---|---|
| id | bigint PK | |
| job_id | bigint FK(backtest_job.id) | |
| metrics_json | jsonb | CAGR/MDD/승률/수익팩터 |
| equity_curve_json | jsonb nullable | 선택 |
| created_at | timestamptz | |

제약:
- UNIQUE(job_id)

---

## 6. 마이그레이션/스키마 변경 규칙

- 스키마 변경은 반드시 ADR 또는 변경 로그를 남긴다.
- 유니크/인덱스는 “성능 최적화”가 아니라 “정합성 스펙”이므로 임의 변경 금지.
