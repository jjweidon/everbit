# Everbit v2 (MVP) UI 구현 명세서 (Next.js App Router)
Last updated: 2026-03-05 (Asia/Seoul)


문서 기준:
- UI/UX 토큰/톤: `docs/design/ui-ux-concept.md`
- 기능 범위(P0): `docs/requirements/functional.md`
- 세션 전략: ADR-0007
- 주문 파이프라인(상태/멱등/UNKNOWN/429/418): `docs/architecture/order-pipeline.md`
- 데이터 모델/용어: `docs/architecture/data-model.md`, `docs/db/schema-v2-mvp.sql`
- 푸시 알림 API/딥링크: `docs/architecture/push-notifications.md`

본 문서는 **프론트(Next.js) 구현 기준**을 고정한다.

---

## 0. 레포/경로 매핑 (everbit monorepo v2: `client/`, `server/`)

v2는 기존 v1을 재사용하지 않고 **덮어쓰기(overwrite)**로 진행한다. 레포 구조는 아래로 고정한다.

- 프론트(Next.js App Router): `client/`
  - 패키지 매니저: pnpm (v2부터 pnpm으로 전환)
  - 토큰 SoT: `docs/ui/tokens.css`
  - 런타임 반영 파일: `client/src/styles/tokens.css` (SoT를 복사/동기화)
  - 전역 스타일 import: `client/src/app/globals.css`에서 `@import "../styles/tokens.css";`
- 백엔드(Spring Boot): `server/`
- Cursor Rules: `.cursor/rules/everbit-v2.md` (토큰 강제/UX 안전 규칙 강제)

> 이 문서에서 `app/`, `styles/` 등은 **Next 앱 루트(= `client/`) 기준의 상대 경로**로 해석한다.

---


## 1. 핵심 UX 원칙(강제)

1) **리스크 우선 가시화**
- `UNKNOWN`, `SUSPENDED`, `429(THROTTLED)`, `418(BLOCKED)`는 화면 어디서든 1초 내 인지 가능해야 한다.

2) **Kill Switch는 전역 고정**
- 어떤 페이지에서도 상단 헤더에 표시.
- OFF 전환은 confirm(1회) + 감사로그 링크(또는 이벤트 로그 링크).

3) **UNKNOWN 자동 재주문 유도 금지**
- UI에 “재주문/재시도” 같은 버튼을 기본 제공하지 않는다.
- 제공 가능 액션은 **reconcile 트리거** 및 **SUSPENDED 해제(수동)** 만.

4) **DB SoT 노출 철학**
- Signal → OrderIntent → OrderAttempt → UpbitOrder/Fill을 “있는 그대로” 단계별로 보여준다.
- 사용자가 추측하지 않도록 상태머신을 UI로 노출(칩 + 타임라인).

---

## 2. 라우팅(Next.js App Router) 구조

> 딥링크 규격(푸시): `/orders/{upbitUuid}` 고정.

권장 폴더 구조:

```
app/
  (auth)/
    login/page.tsx
    callback/page.tsx                # OAuth 후 세션 확립(Refresh 호출)

  (app)/
    layout.tsx                       # AppShell(사이드바+헤더)

    dashboard/page.tsx
    markets/page.tsx
    strategy/page.tsx

    orders/page.tsx
    orders/[upbitUuid]/page.tsx

    backtests/page.tsx
    backtests/[jobPublicId]/page.tsx

    notifications/page.tsx

    settings/
      upbit-key/page.tsx
      security/page.tsx              # (선택) CSRF/Origin 정책 안내, 토큰 상태
      audit/page.tsx                 # (선택) 감사 로그 뷰

  api/
    health/route.ts                  # (선택) vercel/next 내부 헬스

public/
  sw.js                              # Web Push service worker
styles/
  tokens.css
```

네비게이션(사이드바):
- Dashboard
- Markets
- Strategy
- Orders
- Backtests
- Notifications
- Settings (Upbit Key)

---

## 3. 세션/인증 UX (ADR-0007 준수)

### 2.1 토큰 저장 규칙
- Access Token: **메모리(React Context)**
- Refresh Token: **HttpOnly + Secure 쿠키**(서버 설정)

### 2.2 로그인 플로우(권장)
1) `/login`에서 “카카오 로그인” 클릭
2) 서버 OAuth 완료 후 프론트 `/callback`로 리다이렉트
3) `/callback` 페이지에서 `POST /api/v2/auth/refresh` 호출(쿠키 기반)
4) 응답으로 Access Token 수신 → 메모리 저장 → `/dashboard` 이동

### 2.3 API 호출 표준
- 모든 API 요청에 `Authorization: Bearer <access>`
- 401 수신 시:
  - `POST /api/v2/auth/refresh` 1회 시도
  - 성공: 토큰 교체 후 원 요청 1회 재시도
  - 실패: `/login` 리다이렉트

> 쿠키 기반 엔드포인트(refresh/logout)는 `credentials: 'include'`를 강제한다.

---

## 4. 디자인 토큰/테일윈드 적용

### 3.1 Neutral-Black 팔레트(요구사항 반영)

요구: **가장 검은 영역은 `#0A0A0A`**, 기본 배경은 `#111214` 수준으로 유지하고, 푸른 톤은 “거의 안 느껴지도록” 억제한다.  
토큰 이름은 유지하고 **값만 교체**한다(기존 코드/클래스 영향 최소화).

- `--bg-0`: `#0A0A0A` (deepest)
- `--bg-1`: `#111214` (default surface)
- `--bg-2`: `#17181B` (elevated)
- `--border`: `#DCDEE4` (고대비 밝은 흰색)
- `--divider`: `#222326`
- `--text-1`: `#E8E9EB`
- `--text-2`: `#B6BAC3`
- `--text-3`: `#7C828F`

> 포인트 컬러(특히 `cyan`)는 정보/상태 강조 목적이므로 유지한다.


- 색상 하드코딩 금지 → CSS 변수 토큰 사용
- 상태 색 규칙: Green/Red/Yellow/Cyan 의미 고정

필수 파일:
- `styles/tokens.css` (CSS 변수)
- `tailwind.config.ts`에서 `rgb(var(--token) / <alpha-value>)` 패턴으로 바인딩

---

## 5. 공통 UI 컴포넌트 카탈로그

아래 컴포넌트는 **전체 화면에서 동일 규칙**으로 사용한다.

### 4.1 Layout
- **AppShell**
  - props: `{ children }`
  - 구성: `<SidebarNav/> + <TopBar/> + <main/>`

- **SidebarNav**
  - 현재 라우트 강조, 아이콘+텍스트

- **TopBar(Global Status Bar)**
  - 항상 표시 항목:
    - Account Kill Switch Toggle
    - Upbit Key 상태(등록됨/미등록/검증실패)
    - WS 상태(Connected/Degraded/Disconnected)
    - 429/418 카운트/차단 만료시각(있다면)
    - 최신 오류(최신 1건)로 바로가기

### 4.2 Status / Risk
- **StatusChip**
  - props: `{ tone: 'green'|'red'|'yellow'|'cyan'|'neutral', label: string, icon?: ReactNode }`
  - 색상만으로 의미 전달 금지(텍스트 필수)

- **KillSwitchToggle**
  - props: `{ enabled: boolean, onToggle(next:boolean): void, pending?: boolean }`
  - OFF 전환 시 ConfirmDialog 필수

- **SeverityBanner**
  - UNKNOWN/SUSPENDED/418 발생 시 상단(본문 시작) 배너로 추가 노출

### 4.3 Data
- **MetricCard**
  - props: `{ label, value, delta?, footnote? }`

- **DataTable**
  - 기능: 정렬/필터/페이지네이션
  - 숫자 우측 정렬 + `tabular-nums`

- **DetailDrawer(Sheet)**
  - 리스트에서 row 클릭 시 우측 드로어로 상세를 열고,
  - 별도 상세 페이지(`/orders/[upbitUuid]`)도 유지한다(딥링크 대응).

- **Timeline**
  - Signal → Intent → Attempt 상태 전이를 시간순으로 표현

### 4.4 Form
- **TextInput / PasswordInput / NumberInput**
- **Toggle / Select / SegmentedControl**
- **FormHint / FormError**
- **ConfirmDialog(Destructive)**

---

## 6. 페이지별 상세 명세

### 5.1 로그인(`/login`)

**목표**: 단일 CTA로 OAuth 진입.

UI:
- 카카오 로그인 버튼 1개
- 안내:
  - “최초 로그인 계정이 OWNER로 고정”
  - “다른 계정은 차단됨(403)”

상태:
- 로그인 실패(403/401/500) 시 에러 카드 + 재시도

---

### 5.2 Upbit 키 관리(`/settings/upbit-key`)

FR: FR-UPBIT-KEY-001~003

UI 섹션:
1) 현재 상태 카드
- 등록 여부
- 마지막 검증 시각
- 검증 결과 코드/사유(민감값 제외)

2) 키 등록 폼
- Access Key, Secret Key
- 등록 시 즉시 검증(서버)

3) 키 폐기(Destructive)
- confirm 1회
- 폐기 후 “실거래 기능 비활성화” 배너 표시

권장 API(프론트 요구):
- GET `/api/v2/upbit/key/status`
- POST `/api/v2/upbit/key` (등록+검증)
- DELETE `/api/v2/upbit/key`

---

### 5.3 대시보드(`/dashboard`)

FR: FR-UI-001~002, FR-TRADE-005, FR-ORDER-003~004

상단(항상):
- Global Status Bar(TopBar)

본문 그리드(권장 3열):
1) **Execution / Risk**
- account kill switch 상태
- strategy enabled 상태
- lastErrorAt / lastReconcileAt
- UNKNOWN count, SUSPENDED markets, 429/418 상태 요약

2) **Equity / PnL**
- equity
- realized/unrealized
- 최근 24h 변화

3) **Recent Orders (ACK 기준)**
- 최근 20건
- 필드: time, market, side, intentType, attemptStatus, upbitUuid
- row 클릭 → Drawer / 상세 페이지 이동

하단:
- Market 상태 테이블(Enable, Position, Cooldown, Suspended)

권장 API:
- GET `/api/v2/dashboard/summary`
- GET `/api/v2/orders?limit=20&onlyAcked=true`
- GET `/api/v2/markets` (대시보드 하단 마켓 상태 포함; 계약 SoT: `docs/api/contracts.md`)

---

### 5.4 마켓(`/markets`)

FR: FR-TRADE-001

목표:
- 활성 마켓 enable/disable
- priority 조정
- SUSPENDED 관리(수동 해제만)

테이블 컬럼:
- market
- enabled(toggle)
- priority(number)
- positionStatus(FLAT/OPEN)
- tradeStatus(ACTIVE/SUSPENDED)
- lastSignalAt
- cooldownUntil
- action: “SUSPENDED 해제”(조건부)

UX 규칙:
- enabled OFF는 즉시 반영(저장)
- priority 변경은 inline edit + Save
- SUSPENDED 해제는 ConfirmDialog + 사유 입력(권장)

권장 API:
- GET `/api/v2/markets`
- PUT `/api/v2/markets/{market}`
- POST `/api/v2/markets/{market}/unsuspend`

---

### 5.5 전략 설정(`/strategy`)

FR: FR-TRADE-002~004

화면 구성(좌: 그룹 / 우: 폼):
- 그룹: Timeframes, Regime, Entry, Exit, Risk, Execution
- 우측: 각 파라미터 폼(숫자/토글/셀렉트)

필수 표시:
- `strategyKey=EXTREME_FLIP`
- `config_version`
- 마지막 업데이트 시각

추가 기능(권장):
- “JSON 보기/복사” (config_json 노출)
- “기본값으로 리셋”(confirm)

검증 규칙(프론트 최소):
- 금액/비율/바 수는 음수 금지
- maxOpenMarkets >= 1
- min/max 주문금액: min <= max

권장 API(계약 SoT: `docs/api/contracts.md`):
- GET `/api/v2/strategy/config`
- PUT `/api/v2/strategy/config`

---

### 5.6 주문 목록(`/orders`)

FR: FR-ORDER-001~004

기본은 “ACK/UNKNOWN/SUSPENDED/THROTTLED”를 빠르게 탐지하는 운영 화면.

필터:
- market
- intentType
- attemptStatus
- 기간(최근 24h/7d/Custom)

테이블(요약 뷰):
- createdAt
- market
- side
- intentType
- attemptStatus(최신 Attempt)
- attemptNo(최신)
- nextRetryAt(THROTTLED)
- upbitUuid(있으면)
- reasonCode

Row 클릭:
- Drawer: Intent/Attempt 타임라인 + 에러 메시지 + 관련 Signal 링크
- 버튼:
  - “상세 보기” → `/orders/[upbitUuid]` (upbitUuid 없으면 Attempt 기반 상세로 이동하는 별도 라우트도 가능)

권장 API:
- GET `/api/v2/orders` (read-model: intent+latest attempt join)

---

### 5.7 주문 상세(`/orders/[upbitUuid]`)

딥링크(푸시) 규격 대응 화면.

섹션:
1) UpbitOrder 스냅샷
- upbitUuid
- state(wait/done/cancel)
- ordType, side
- price/volume/executedVolume

2) 관련 Intent
- intentType, requestedKrw/Volume, reasonCode

3) Attempt 타임라인
- attempt_no 별 상태(PREPARED→SENT→...)
- 429이면 nextRetryAt
- UNKNOWN이면 reconcile 결과/마켓 tradeStatus=SUSPENDED 전환 여부 표시

4) Fill(체결)
- trade_time, price, volume, fee

권장 API:
- GET `/api/v2/orders/{upbitUuid}` (order+fills+intent+attempts)

---

### 5.8 백테스트(`/backtests`, `/backtests/[jobPublicId]`)

FR: FR-BT-001~002

목록:
- jobPublicId
- status(QUEUED/RUNNING/DONE/FAILED)
- createdAt
- markets/timeframes(요약)

실행 폼:
- markets(multi)
- timeframes(multi)
- period(from/to)
- initialCapital
- fee/slippage

상세:
- metrics(CAGR, MDD, winRate, profitFactor)
- equity curve(라인)
- request_json/strategy version 표시

권장 API:
- GET `/api/v2/backtests`
- POST `/api/v2/backtests`
- GET `/api/v2/backtests/{jobPublicId}`

---

### 5.9 알림(`/notifications`)

FR: FR-NOTI-001~003

섹션:
1) 권한 상태 카드
- `Notification.permission` 기반
- denied면 브라우저 설정 가이드

2) 푸시 토글
- ON: sw 등록 → subscribe 생성 → POST 구독 등록
- OFF: 서버 구독 해지 + (가능하면) 브라우저 unsubscribe

3) 구독 목록
- endpoint 마스킹(앞/뒤 일부만)
- userAgent
- enabled

4) 테스트 푸시
- message + deepLink 입력
- 결과(요청/성공/실패) 표시

API(계약 SoT: `docs/api/contracts.md`, 상세 DTO: `docs/architecture/push-notifications.md` §4):
- POST `/api/v2/push/subscriptions`
- GET `/api/v2/push/subscriptions`
- DELETE `/api/v2/push/subscriptions/{id}`
- POST `/api/v2/push/test`

---

## 6. 상태/이벤트 매핑(칩/배너)

### 6.1 주문 Attempt 상태 → UI
- PREPARED: Neutral
- SENT: Cyan
- ACKED: Green
- REJECTED: Red
- THROTTLED(429): Yellow(“429”) + nextRetryAt
- UNKNOWN: Yellow(“UNKNOWN”) + 배너
- SUSPENDED: Yellow(“SUSPENDED”) + 배너

### 6.2 마켓 상태
- `positionStatus`와 `tradeStatus`는 분리한다.
  - `positionStatus`: FLAT / OPEN
  - `tradeStatus`: ACTIVE / SUSPENDED
- `tradeStatus = SUSPENDED` 인 경우:
  - Markets 페이지에서만 “해제” 액션 제공(실수 방지)

---

## 7. 프론트-백 API 계약(초안)

**API 계약 SoT**: `docs/api/contracts.md`. 아래 DTO 예시는 계약 문서와 동일해야 한다.

현재 문서에 명시된 API는 Push 영역만이다. 나머지는 프론트 구현을 위해 아래 read-model 중심으로 정리한다.

원칙:
- P0는 “운영 화면”이 목적이므로, join이 많은 조회는 서버에서 read-model DTO로 제공한다.
- 응답에는 bigint 내부 id를 노출하지 않는다(public_id/uuid 사용).

권장 DTO(예시):

### DashboardSummary
```ts
type DashboardSummary = {
  accountEnabled: boolean;
  strategyKey: 'EXTREME_FLIP';
  strategyEnabled: boolean;
  wsStatus: 'CONNECTED'|'DEGRADED'|'DISCONNECTED';
  lastReconcileAt?: string;
  risk: {
    throttled429Count24h: number;
    blocked418Until?: string;
    unknownAttempts24h: number;
    suspendedMarkets: string[];
  };
  equity: {
    equityKrw: string;
    realizedPnlKrw: string;
    unrealizedPnlKrw: string;
  };
}
```


### MarketItem
```ts
type MarketItem = {
  market: string;
  enabled: boolean;
  priority: number;
  positionStatus: 'FLAT'|'OPEN';
  tradeStatus: 'ACTIVE'|'SUSPENDED';
  suspendReasonCode?: string;
  lastSignalAt?: string;
  cooldownUntil?: string;
}
```

### OrderListItem (Intent + Latest Attempt)
```ts
type OrderListItem = {
  intentPublicId: string;     // UUID v7
  createdAt: string;
  market: string;
  side: 'BUY'|'SELL';
  intentType: 'ENTRY'|'EXIT_STOPLOSS'|'EXIT_TP'|'EXIT_TRAIL'|'EXIT_TIME';
  requestedKrw?: string;
  requestedVolume?: string;
  reasonCode?: string;
  latestAttempt: {
    attemptPublicId: string;
    attemptNo: number;
    status: 'PREPARED'|'SENT'|'ACKED'|'REJECTED'|'THROTTLED'|'UNKNOWN'|'SUSPENDED';
    upbitUuid?: string;
    nextRetryAt?: string;
    errorCode?: string;
  };
}
```

---

## 8. 구현 체크리스트(P0)

- [ ] tokens.css + tailwind binding 적용
- [ ] AppShell/Sidebar/TopBar 고정 레이아웃
- [ ] Kill Switch confirm + 반영
- [ ] Upbit 키 등록/검증/폐기 UX
- [ ] Orders: list + detail + Attempt 타임라인
- [ ] Markets: enable/priority + SUSPENDED 해제
- [ ] Push: permission/subscribe/unsubscribe/test
- [ ] 401 자동 refresh + 1회 retry

