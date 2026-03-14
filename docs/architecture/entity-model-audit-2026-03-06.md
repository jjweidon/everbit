# 엔티티/ERD 감사 메모

Status: **Applied**  
Owner: everbit  
Last updated: 2026-03-11 (Asia/Seoul)

이 문서는 2026-03-06 기준으로 v2 MVP 문서를 점검하면서 반영한 **엔티티/ERD 보완 사항**을 요약한다.

## 반영된 핵심 수정

### 1. BaseEntity 기준으로 감사 컬럼 통일
- 모든 엔티티 테이블에 `created_at`, `updated_at`를 모두 두도록 문서를 정렬했다.
- append-only row라도 `updated_at`을 유지하도록 고정했다.
- 비즈니스 시각(`captured_at`, `trade_time`, `rotated_at` 등)은 별도 컬럼으로 유지한다.

### 2. 시장 실행 상태와 포지션 상태 분리
기존 문서에는 `position.status = SUSPENDED`가 섞여 있어 의미가 모호했다.

반영:
- `position.status`: `FLAT`, `OPEN`
- `market_state.trade_status`: `ACTIVE`, `SUSPENDED`

효과:
- “보유 중이지만 현재 자동매매는 중단된 마켓”을 정확히 표현할 수 있다.
- `/markets` 응답과 UI에서도 `positionStatus`와 `tradeStatus`를 분리했다.

### 3. 부모-자식 owner 불일치 방지
`owner_id`를 중복 보유하는 테이블은 조회에는 유리하지만, parent owner와 어긋나면 데이터 오염이 발생할 수 있다.

반영:
- `order_intent(signal_id, owner_id) -> signal(id, owner_id)`
- `order_attempt(order_intent_id, owner_id) -> order_intent(id, owner_id)`
- `fill(upbit_uuid, owner_id) -> upbit_order(upbit_uuid, owner_id)`

### 4. Kill Switch와 SUSPENDED 의미 분리
기존 문서에는 “Attempt 생성 금지”와 “executor가 SKIPPED 처리”가 동시에 있어 충돌이 있었다.

반영:
- Kill Switch OFF면 신규 Attempt/outbox 발행 금지
- race로 이미 PREPARED Attempt가 executor에 도달했을 때만 `order_attempt.status = SUSPENDED`
- 시장 단위 안전 중단은 `market_state.trade_status = SUSPENDED`

### 5. 명시적 CHECK 제약 추가
모호했던 컬럼 의미를 DB 제약으로 보강했다.

예:
- `order_intent`: BUY면 `requested_krw`, SELL이면 `requested_volume`
- `order_attempt`: ACKED면 `upbit_uuid` 필수, THROTTLED면 `next_retry_at` 필수
- 수량/단가/캔들 값의 음수 입력 방지

### 6. 교차 문서 충돌 정리
- `spring-boot-conventions.md`의 snake_case DTO 예시를 camelCase 기준으로 수정
- `functional.md`의 Signal 유니크 키 정의에 `owner_id` 누락된 부분 보정
- `event-bus.md`의 `notification_log` dedupe 키 설명을 실제 스키마(`owner_id, event_id`)에 맞춰 수정

## 여전히 의도적으로 FK를 두지 않은 관계
다음은 **히스토리 보존**을 위해 물리 FK를 두지 않았다.
- `signal.strategy_key` ↔ `strategy_config`
- `signal.market` ↔ `market_config`
- `upbit_order` ↔ `order_attempt`

사유:
- 과거 히스토리 row가 현재 설정/운영 상태 row의 삭제 또는 재생성에 종속되면 안 되기 때문
- `upbit_order`는 reconcile/외부 유입 주문까지 수용할 수 있어 물리 1:1 강제가 과도함

## 구현 시 우선 적용 순서
1. DDL 반영 (`docs/db/schema-v2-mvp.sql`)
2. `BaseEntity`/Auditing 적용
3. `MarketState` 엔티티 추가 및 `/markets` read-model 수정
4. `OrderIntent` / `OrderAttempt` / `Fill` 복합 FK 매핑 테스트
5. Kill Switch race/UNKNOWN/SUSPENDED 회귀 테스트 보강
