# 컴포넌트/모듈 경계

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- “어디에 어떤 코드가 있어야 하는지”를 고정한다.
- 모듈 간 결합도를 낮춰, AI 생성 코드가 시스템 경계를 침범하지 않도록 한다.

---

## 1. 설계 원칙(강제)

1) **SoT는 PostgreSQL**
- 상태의 최종 기준은 DB다. Kafka/Redis는 보조.

2) **외부 연동은 Adapter로 격리**
- Upbit/Kakao/WebPush는 전부 Adapter 계층 뒤로 숨긴다.

3) **숨은 부작용 금지**
- 함수/모듈 이름으로 예측 가능한 동작만 수행한다.
- 로깅/메트릭/알림 전송은 호출자가 명시적으로 트리거한다(또는 이벤트로 분리).

4) **조건 분기 복잡도는 컴포넌트/모듈로 분리**
- 서로 다른 정책(레짐/주문타입/리스크)은 단일 함수에서 뭉개지 않는다.

---

## 2. Backend(Sprint Boot) 모듈 경계

> 패키지 예시는 가이드다. 핵심은 “책임”과 “입출력”이다.

### 2.1 API Layer
- `AuthController`, `UpbitKeyController`, `StrategyConfigController`, `TradingController`, `BacktestController`, `PushController`
- 책임:
  - 요청/응답 DTO
  - 인증/인가(OWNER-only)
  - 입력 검증(형식/범위)
- 금지:
  - Upbit 직접 호출
  - DB 트랜잭션 복잡 로직(서비스로 이동)

### 2.2 Domain/Service Layer
#### Trading
- `StrategyEngine`
  - 입력: market/timeframe candle close data
  - 출력: Signal(구조화된 이유/강도 포함)
- `RiskEngine`
  - 입력: Signal + 현재 Position + 설정
  - 출력: OrderIntent 생성 여부 + intent_type + ReasonCode
- `OrderIntentService`
  - Signal/Intent 저장, Outbox 생성
- `OrderStateService`
  - Order/Fill/Position/PnL 업데이트(정합성 유지)

#### Execution
- `OrderExecutor`
  - 입력: orderAttemptId
  - 동작: Upbit 주문 생성/조회/취소 수행(반드시 Adapter 경유)
  - 출력: Attempt 상태 전이 + 이벤트(outbox)

#### Reconciliation
- `ReconcileService`
  - 입력: 트리거(부팅/재연결/주기)
  - 동작: open orders/balance 동기화(저빈도)

#### Backtest
- `BacktestService`(요청 저장/조회)
- `BacktestWorker`(Kafka 소비/시뮬레이션)

#### Notification
- `NotificationService`
  - 입력: 도메인 이벤트(OrderAccepted 등)
  - 동작: WebPush 전송(구독 조회/정리 포함)
  - 출력: NotificationLog 저장(선택)

### 2.3 Adapter Layer(외부 연동)
- `UpbitClient`(REST)
- `UpbitWsClient`(WebSocket public/private)
- `KakaoOAuthClient`
- `WebPushClient`

규칙:
- Adapter는 “외부 프로토콜/에러/레이트리밋/재시도”를 내부 도메인에 누수시키지 않는다.
- 도메인은 Adapter의 결과를 “명시적 타입”으로 받는다(예: `UpbitError { code, httpStatus, retryable }`).

### 2.4 Persistence Layer
- Repository/JPA/Query 계층
- 중요한 테이블: Signal, OrderIntent, OrderAttempt, UpbitOrder, Fill, Position, Pnl, Outbox, StrategyConfig, PushSubscription

규칙:
- 유니크 제약/인덱스는 `docs/architecture/data-model.md`가 최종 기준이다.
- 멱등은 “쿼리 if exists”가 아니라 **유니크 제약 + 예외 처리**로 보장한다.

---

## 3. Frontend(Next.js) 구조 가이드

목표:
- 기능 단위로 폴더를 묶어서 삭제/수정/확장이 쉽도록 한다.
- 로직/상태/컴포넌트를 한 파일에서 과도하게 섞지 않는다.

### 3.1 디렉터리 구조(권장)
- 기능/도메인 중심으로 묶는다.

예:
```
client/src/
  shared/                # 공통 컴포넌트/유틸/훅
  domains/
    auth/                # 로그인/세션
    trading/             # 실행/킬스위치/마켓 설정
    backtest/            # 백테스트 실행/결과
    dashboard/           # 상태/주문/잔고/손익
    notifications/       # 푸시 권한/구독 관리
```

### 3.2 상태/훅 설계 원칙
- Zustand store는 “범위가 작은 store 여러 개”로 쪼갠다(거대 store 금지).
- API 훅은 가능한 한 “일관된 반환 형태”를 유지한다.
- 복잡한 조건은 이름 있는 변수/정책 객체로 분리한다.

### 3.3 UI 분기 원칙
- 상태별 UI가 크게 다르면 컴포넌트를 분리한다.
  - 예: `PushPermissionBanner` / `PushEnabledPanel` / `PushBlockedPanel`

### 3.4 매직 넘버 금지
- 시간(ms), 재시도 횟수, threshold 등은 상수로 명명한다.
- 상수는 사용되는 로직 근처(도메인 폴더) 또는 `shared/constants`에 둔다.

---

## 4. 푸시 알림(클라이언트) 구성(요약)

- Service Worker 등록(명시적 사용자 액션 이후)
- Push subscription 생성(PushManager)
- 서버에 구독 등록/해지 API 호출
- 알림 수신 시:
  - 알림 클릭 → 대시보드/주문 상세로 이동(딥링크)
  - 동일 eventId 중복 표시 방지(선택)

서버 쪽 상세는 `docs/architecture/order-pipeline.md`, `docs/architecture/data-model.md`를 따른다.
