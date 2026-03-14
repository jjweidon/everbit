# EXTREME_FLIP(극점포착) 전략 스펙 v1.0

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)  
Target: Upbit Spot(현물), **Long-only**  
Execution: 전략 1개 + 여러 마켓(동시 포지션 최소화)

---

## 0. 요약

EXTREME_FLIP은 **고점/저점(극점)** 을 기술적 지표 기반으로 탐지하되, **Flip(반전 확인)** 없이는 진입/청산을 실행하지 않는다.  
특히 v1에서 발생했던 문제(장기 하락 구간에서 단기 저점 신호를 따라 **추격매수 → 손실 누적**)를 방지하기 위해, v2에서는 **장·단기 레짐(추세 상태)** 를 먼저 분류하고 레짐별로:

- 단기 극점 포착(신호 빈도)
- 진입 허용 조건(Flip/다이버전스/매크로 극점)
- 쿨다운/재진입 제한

을 달리 적용한다.

핵심 목표는 “단기 저점 잘 찍기”가 아니라, **장기적으로 하락하는 레짐에서는 추격을 차단**하고, **극단 구간에서만 제한적으로 수익을 수확**하는 것이다.

---

## 1. 목표 / 비목표

### 1.1 목표
- 단기/장기 관점에서 **유동적인** 극점 정의(레짐별 임계값)
- DOWN_TREND에서 추격매수/연속 진입을 구조적으로 차단
- 모든 의사결정은 Reason Code로 기록(백테스트/운영 재현 가능)

### 1.2 비목표
- 초단타(틱 기반) 최적화
- 레버리지/마진/파생
- 멀티 계정/멀티 유저

---

## 2. 데이터 / 타임프레임(결정 고정)

- Signal TF(단기): `15m`
- Macro TF(중기 레짐): `4h`
- Long TF(장기 필터): `1d`

규칙:
- 신호는 **확정 캔들 close** 시점에서만 생성한다(look-ahead 금지).
- 멀티 TF 조합 시, 각 TF의 “마지막 확정 값”만 사용한다.

---

## 3. 레짐(추세 상태) 분류(추격 방지 게이트)

### 3.1 지표
- `EMA200` (close)
- `slope`: EMA200의 기울기(최근 n개 바 차)

기본 파라미터:
- `slopeBars=10`

### 3.2 레짐 판정(결정 고정)

Long TF(1d) 기준:
- `LONG_UP`  : close_1d > EMA200_1d AND slope_1d > 0
- `LONG_DOWN`: close_1d < EMA200_1d AND slope_1d < 0
- else: `LONG_FLAT`

Macro TF(4h) 기준:
- `MACRO_UP`  : close_4h > EMA200_4h AND slope_4h > 0
- `MACRO_DOWN`: close_4h < EMA200_4h AND slope_4h < 0
- else: `MACRO_FLAT`

최종 레짐:
1) `DOWN_TREND`: LONG_DOWN이면 무조건 DOWN_TREND
2) `UP_TREND`: LONG_UP AND MACRO_UP
3) `RANGE`: LONG_FLAT AND MACRO_FLAT
4) `TRANSITION`: 그 외

> DOWN_TREND는 “단기 반등 신호가 많아도” 기본적으로 진입을 막는 레짐이다.

---

## 4. 단기 극점(Extreme) 정의(15m)

### 4.1 공통 지표
- EMA_fast = EMA(close, 20)
- ATR = ATR(14)
- RSI = RSI(14)
- Bollinger(20,2)
  - %B = (close - BB_down) / (BB_up - BB_down)
- dev = (close - EMA_fast) / ATR

### 4.2 Oversold Extreme(저점 후보)
레짐별 임계값(결정 고정):

- UP_TREND: `%B <= 0.15` AND `RSI <= 30` AND `dev <= -1.5`
- RANGE: `%B <= 0.10` AND `RSI <= 28` AND `dev <= -1.8`
- TRANSITION: `%B <= 0.08` AND `RSI <= 26` AND `dev <= -2.1`
- DOWN_TREND(감지 전용): `%B <= 0.05` AND `RSI <= 22` AND `dev <= -2.7`

### 4.3 Overbought Extreme(고점 후보)
레짐별 임계값(결정 고정):

- UP_TREND: `%B >= 0.90` AND `RSI >= 70` AND `dev >= +1.8`
- RANGE: `%B >= 0.95` AND `RSI >= 72` AND `dev >= +2.0`
- TRANSITION: `%B >= 0.95` AND `RSI >= 75` AND `dev >= +2.2`
- DOWN_TREND: 반등 청산 강화(빠른 익절) 용도

---

## 5. 장기 극점(Macro Extreme) 정의

DOWN_TREND에서 진입은 단기 극점만으로 허용하지 않는다.

### 5.1 Macro Oversold Extreme(결정 고정)
Macro(4h) 조건:
- RSI_4h <= 28
- dev_4h <= -2.5

Long(1d) 강화 조건(둘 중 1개):
- RSI_1d <= 35
- 또는 1d 기준 EMA20 대비 -2ATR 이상 이탈

### 5.2 Capitulation(권장)
- volume_15m >= SMA(volume_15m, 20) * 2.0

---

## 6. Divergence(다이버전스) — DOWN_TREND 진입 필수

### 6.1 Pivot low 정의
- pivotLen=3
- pivot low: 양 옆 pivotLen 구간보다 low가 낮을 때

### 6.2 Bullish Divergence(결정 고정)
최근 `divLookbackBars=60(15m)` 내 pivot low 2개를 사용:
- 가격: low2 < low1 * (1 - 0.005)
- RSI: RSI2 > RSI1 + 3

---

## 7. Flip(반전 확인) — 진입 전 필수

### 7.1 TTL(결정 고정)
- flipTtlBars=6(15m 기준 90분)
- TTL 내 Flip 미성공 시 Extreme 감지는 만료

### 7.2 Flip Confirm(BUY) 2-of-4(결정 고정)
아래 조건 중 2개 이상:
1) close가 직전 캔들 고가 상향 돌파
2) RSI >= 32 회복
3) close > EMA_fast
4) dev 개선(dev[t]-dev[t-1] >= 0.5)

### 7.3 Flip Confirm(SELL 강화) 2-of-3
1) close가 직전 캔들 저가 하향 돌파
2) RSI <= 68
3) close < EMA_fast

---

## 8. 진입 규칙(결정 고정)

### 8.1 공통 전제
- Kill Switch(계정/전략) ON
- 마켓 enabled
- 주문 min/max 충족
- 동일 마켓 중복 포지션 금지
- 쿨다운이면 진입 금지

### 8.2 레짐별 진입

UP_TREND:
- Oversold Extreme + Flip Confirm(BUY)
- 추격 방지: close >= rollingHigh(20) * 0.995이면 진입 금지

RANGE:
- Oversold Extreme + Flip Confirm(BUY)

TRANSITION:
- UP_TREND 규칙과 동일하되 포지션 사이즈 스케일 0.7 적용

DOWN_TREND(캡틸레이션 반등만):
- 단기 Oversold Extreme
- Macro Oversold Extreme
- Bullish Divergence(필수)
- Flip Confirm(BUY)
- (권장) Capitulation

추가 제약:
- downtrendCooldownMacroBars=6(4h 기준 24h) 재진입 금지
- maxHoldingBarsDown=16(15m 기준 4h) 시간청산 intent 생성

---

## 9. 멀티 마켓 동시성 / 우선순위

### 9.1 동시 포지션 제한
- maxOpenMarkets=2(기본)
- 동시 BUY 후보 발생 시 strength 점수 상위 N개만 채택

### 9.2 유동성 필터(필수)
- 24h 거래대금 하한: `minAccTradePrice24hKRW = 3_000_000_000`

### 9.3 Strength 점수(결정 고정)
- strength = (abs(dev)*0.5) + ((0.5-%B)*2.0) + ((30-RSI)*0.05)
- DOWN_TREND 가중치:
  - +1.0(MacroExtreme)
  - +1.0(Divergence)
  - +0.5(Capitulation)

---

## 10. 추격매수 방지(강제)

- 손실 상태 추가매수(DCA) **금지**
- 피라미딩(수익 중 추가) 기본 OFF
- StopLoss 후 쿨다운:
  - UP/RANGE/TRANSITION: 12 bars(15m 기준 3h)
  - DOWN_TREND: 6 bars(4h 기준 24h)

---

## 11. 리스크/청산 정책(전략 권고값)

UP/RANGE/TRANSITION:
- SL: entry - 2.0*ATR
- TP1(부분 40%): entry + 2.0*ATR
- 나머지 trailing:
  - start: entry + 2.0*ATR
  - trail: highest(close) - 2.0*ATR

RANGE 추가:
- TP1(부분 50%): BB_mid 터치
- TP2: SELL 강화 신호 또는 trailing

DOWN_TREND:
- SL: entry - 1.5*ATR
- TP1(부분 60%): close >= EMA_fast 또는 entry + 1.2*ATR
- 잔량 trailing:
  - start: entry + 1.2*ATR
  - trail: highest(close) - 1.5*ATR
- 시간청산: maxHoldingBarsDown 초과 시 강제 청산 intent 생성

---

## 12. 주문 실행 정책(운영 안정성 우선)

주문/재시도/UNKNOWN/418 처리의 최종 기준은 `docs/architecture/order-pipeline.md`를 따른다.

### 12.1 Attempt/identifier
- 주문 실행은 OrderAttempt 단위다.
- Attempt마다 identifier는 신규 생성(재사용 금지).
- timeout/5xx는 UNKNOWN으로 수렴하며 자동 재주문 금지.

### 12.2 주문 타입(권장)
- Entry(BUY): best + FOK (전량 체결 아니면 포기)
- Profit exit(SELL): best + IOC (일부 체결 허용)
- Emergency exit(손절/비상): market sell

---

## 13. 상태 머신(마켓별)

- IDLE
- EXTREME_WATCH(Flip 대기, TTL)
- IN_POSITION
- COOLDOWN

전이:
- IDLE → EXTREME_WATCH: ExtremeDetected
- EXTREME_WATCH → IN_POSITION: FlipConfirmed + 레짐 게이트 통과 + 주문 ACK
- EXTREME_WATCH → IDLE: TTL 만료/레짐 변경
- IN_POSITION → COOLDOWN: StopLoss/시간청산/비상청산
- IN_POSITION → IDLE: 정상 익절 완료

---

## 14. Reason Codes(필수)

- EXTREME_DETECTED_OS / EXTREME_DETECTED_OB
- FLIP_CONFIRMED_BUY / FLIP_CONFIRMED_SELL_BIAS
- REGIME_BLOCKED_DOWN_TREND
- MACRO_EXTREME_MISSING / DIVERGENCE_MISSING / CAPITULATION_MISSING
- COOLDOWN_BLOCKED / LIQUIDITY_BLOCKED
- RISK_STOPLOSS_TRIGGERED / RISK_TAKEPROFIT_TRIGGERED / RISK_TRAILING_TRIGGERED
- TIME_EXIT_DOWN_TREND

---

## 15. 백테스트 검증(필수 시나리오)

- v1 실패 재현: 지속 하락 + 단기 반등 반복 구간
  - 기대: DOWN_TREND 거래 빈도 급감, 연속 진입 차단
- 횡보장: RANGE 평균회귀 성과
- 강한 상승장: 추격 없이 눌림 진입 + trailing 수익 확대
