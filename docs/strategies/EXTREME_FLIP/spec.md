# EXTREME_FLIP(극점포착) 전략 스펙 v1.0

Status: **Ready for Implementation**  
Owner: everbit  
Last updated: 2026-02-14  
Target Exchange: Upbit Spot (현물)  
Position: **Long-only** (공매도/숏 없음)  
Execution: **전략 1개 + 여러 마켓**, 단 동시 포지션은 전략이 최소화(기본 `maxOpenMarkets=2`)

---

## 0. 요약

EXTREME_FLIP은 **단기 극점(과매도/과매수)** 을 포착하되, **Flip(반전 확인)** 없이는 진입하지 않는다.  
또한 **장기 하락 추세(DOWN_TREND)** 에서는 “단기 저점 신호”를 무시하고, **장기 과매도 극단 + 다이버전스 + 반전 확인**이 동시에 성립하는 경우에만 제한적으로 진입한다.  
즉, v1에서 발생했던 “지속 하락장에서 추격매수 → 손실 누적”을 구조적으로 차단한다.

---

## 1. 목표 / 비목표

### 1.1 목표
- 단기/장기 관점에서 **유동적 극점**을 정의하고, 극점에서만 진입/청산하여 수익을 극대화한다.
- 장기 하락장에서는 거래를 희소화하고, **캡itulaton(과매도 극단) 반등 구간**만 수확한다.
- 모든 신호/의사결정은 **Reason Code**로 기록되어 백테스트/운영에서 재현 가능해야 한다.

### 1.2 비목표
- 고주파/초단타(틱 기반) 최적화는 범위 밖.
- 레버리지/마진/파생상품 거래는 범위 밖.

---

## 2. 데이터 / 타임프레임 (결정 고정)

- **Signal TF(단기)**: `15m`
- **Macro TF(중기, 레짐 판단)**: `240m(4h)`
- **Long TF(장기 필터)**: `1d`

정렬 규칙(룰):
- 15m 캔들에서 신호를 만들 때, Macro/Long 지표는 **마지막 확정 캔들 값**만 사용한다(look-ahead 금지).

---

## 3. 레짐(추세 상태) 분류 (추격 방지 핵심 게이트)

레짐은 반드시 **진입 판단보다 먼저** 수행한다.

### 3.1 지표 (Macro/Long 공통)
- `EMA200 = EMA(close, 200)`
- `slope = EMA200(t) - EMA200(t - slopeBars)`  
  - 기본 `slopeBars = 10` (해당 TF 기준)

### 3.2 레짐 판정 (결정 고정)

Long TF(1d) 우선:
- `LONG_UP`  : close_1d > EMA200_1d AND slope_1d > 0
- `LONG_DOWN`: close_1d < EMA200_1d AND slope_1d < 0
- 그 외: `LONG_FLAT`

Macro TF(4h) 보조:
- `MACRO_UP`  : close_4h > EMA200_4h AND slope_4h > 0
- `MACRO_DOWN`: close_4h < EMA200_4h AND slope_4h < 0
- 그 외: `MACRO_FLAT`

최종 레짐:
1) `DOWN_TREND` : LONG_DOWN이면 무조건 DOWN_TREND (가장 보수적)
2) `UP_TREND`   : LONG_UP AND MACRO_UP
3) `RANGE`      : LONG_FLAT AND MACRO_FLAT
4) `TRANSITION` : 그 외(애매한 구간)

> 운영 안정성 우선: 장기 하락 신호가 조금이라도 있으면 보수적으로 DOWN_TREND로 분류한다.

---

## 4. 단기 극점(Extreme) 정의 (15m)

### 4.1 공통 지표
- `EMA_fast = EMA(close, 20)`
- `ATR = ATR(14)`
- `RSI = RSI(14)`
- Bollinger(20,2):
  - `BB_mid = SMA(20)`
  - `BB_up/down = mid ± 2*std(20)`
  - `%B = (close - BB_down) / (BB_up - BB_down)`  (0~1)

추가 지표:
- `dev = (close - EMA_fast) / ATR`  (ATR 정규화 거리)

### 4.2 레짐별 Oversold Extreme(저점 후보) (결정 고정)
- **UP_TREND**:
  - `%B <= 0.15` AND `RSI <= 30` AND `dev <= -1.5`
- **RANGE**:
  - `%B <= 0.10` AND `RSI <= 28` AND `dev <= -1.8`
- **TRANSITION**:
  - `%B <= 0.08` AND `RSI <= 26` AND `dev <= -2.1`
- **DOWN_TREND** (단기만으로는 진입 불가, “감지”만 함):
  - `%B <= 0.05` AND `RSI <= 22` AND `dev <= -2.7`

### 4.3 레짐별 Overbought Extreme(고점 후보) (결정 고정)
- **UP_TREND**:
  - `%B >= 0.90` AND `RSI >= 70` AND `dev >= +1.8`
- **RANGE**:
  - `%B >= 0.95` AND `RSI >= 72` AND `dev >= +2.0`
- **TRANSITION**:
  - `%B >= 0.95` AND `RSI >= 75` AND `dev >= +2.2`
- **DOWN_TREND**:
  - “반등 청산 강화”에만 사용(빠른 익절 목적):
  - `%B >= 0.85` OR `RSI >= 60` OR `close >= EMA_fast`

---

## 5. 장기 극점(Macro Extreme) 정의 (4h/1d)

DOWN_TREND에서 진입을 허용하려면 **단기 Oversold Extreme + Macro Extreme + Divergence + Flip**이 모두 필요하다.

### 5.1 Macro Oversold Extreme (결정 고정)
Macro TF(4h) 기준:
- `RSI_4h <= 28`
- `dev_4h <= -2.5`  (dev는 동일 정의를 4h에 적용)

Long TF(1d) 기준(추가 강화, 둘 중 1개만 만족해도 됨):
- `RSI_1d <= 35` OR `close_1d << EMA_fast_1d`(1d EMA20 대비 -2 ATR 이상)

### 5.2 Volume Capitulation (옵션, 권장)
- 15m 또는 4h 기준:
- `volume >= SMA(volume, 20) * 2.0`

---

## 6. Divergence(다이버전스) - DOWN_TREND 진입 필수 (결정 고정)

DOWN_TREND에서 “추격 방지”를 위해 **Bullish Divergence를 필수**로 둔다.

### 6.1 Pivot 정의 (15m)
- `pivotLen = 3`
- pivot low 조건:
  - `low[i] <= min(low[i-1..i-pivotLen])` AND `low[i] < min(low[i+1..i+pivotLen])`

### 6.2 Bullish Divergence 조건
최근 `divLookbackBars = 60` 내 pivot low 2개를 찾는다:
- 가격: `low2 < low1 * (1 - 0.005)`  (최소 0.5% 더 낮은 저점)
- RSI: `RSI2 > RSI1 + 3`  (최소 3p 더 높은 RSI 저점)

> DOWN_TREND에서 단기 과매도 신호만 보고 계속 들어가는 패턴을 차단하기 위한 필수 규칙이다.

---

## 7. Flip(반전 확인) - 진입 전 필수

Extreme 감지는 “대기 상태”로만 처리하고, Flip Confirm가 있어야 주문 Intent를 생성한다.

### 7.1 Flip TTL (결정 고정)
- `flipTtlBars = 6` (15m 기준, 최대 90분 대기)
- TTL 내 Flip 실패 시 감지 무효화(IDLE 복귀)

### 7.2 Flip Confirm (BUY) (결정 고정: 2-of-4)
단기 Oversold Extreme 이후, 아래 4개 중 **2개 이상** 만족하면 Flip Confirm:

1) `close`가 직전 캔들 고가를 상향 돌파
2) `RSI >= 32` 회복(oversold 탈출 확인)
3) `close > EMA_fast`
4) `dev`가 상승 방향으로 개선(예: dev[t] - dev[t-1] >= 0.5)

### 7.3 Flip Confirm (SELL 강화 신호) (결정 고정: 2-of-3)
Overbought Extreme 이후 아래 3개 중 **2개 이상** 만족하면 “청산 강화 신호”:

1) `close`가 직전 캔들 저가 하향 돌파
2) `RSI <= 68`로 재진입(과열 해소)
3) `close < EMA_fast`

---

## 8. 진입 규칙 (결정 고정)

### 8.1 공통 전제
- Kill Switch(계정/전략) ON
- 마켓 활성 리스트에 포함
- 잔고/주문 min-max 규칙 충족
- 동일 마켓 중복 포지션 금지
- 쿨다운 상태면 진입 금지

### 8.2 레짐별 진입

#### 8.2.1 UP_TREND (눌림 매수)
- 단기 Oversold Extreme 감지
- Flip Confirm(BUY) 충족
- 추가 추격 방지:
  - `close >= rollingHigh(20) * 0.995` 이면 진입 금지 (고점 근접 추격 차단)

#### 8.2.2 RANGE (평균회귀)
- 단기 Oversold Extreme + Flip Confirm(BUY) → 진입
- 청산은 “Overbought Extreme + Flip Confirm(SELL 강화)” 또는 Risk Engine TP/Trailing로 처리

#### 8.2.3 TRANSITION (보수 모드)
- UP_TREND 조건과 동일하되, Oversold 조건은 더 빡빡하게(4.2 참조)
- 진입 수량은 `positionSizeScaleTransition = 0.7` 적용(기본 70%)

#### 8.2.4 DOWN_TREND (캡itulaton 반등만)
아래 조건을 **모두** 만족해야 BUY Intent 생성:

- 단기 Oversold Extreme 감지
- Macro Oversold Extreme 충족
- Bullish Divergence 충족(필수)
- Flip Confirm(BUY) 충족
- (권장) Volume Capitulation 충족

추가 제약(필수):
- DOWN_TREND 진입 후 `downtrendCooldownMacroBars = 6` (4h 기준 24시간) 재진입 금지
- 보유 시간 제한: `maxHoldingBarsDown = 16` (15m 기준 4시간)  
  - 시간 초과 시 “이익/손실 무관” 청산 시그널을 Risk Engine에 전달

---

## 9. 멀티 마켓 동시성 / 우선순위 (결정 고정)

### 9.1 동시 포지션 제한
- `maxOpenMarkets = 2` (기본 고정)
- 동시에 2개 이상의 BUY 신호가 발생하면 **점수(Strength) 기반으로 상위 2개만** 선택

### 9.2 유동성 필터 (운영 안정성 필수)
- 24h 누적 거래대금(= traded value) 기준으로 하한을 둔다.
- 기본값:
  - `minAccTradePrice24hKRW = 3_000_000_000` (30억 KRW)
- 데이터는 Upbit Ticker의 `acc_trade_price_24h`를 사용한다. :contentReference[oaicite:0]{index=0}

> 목적: 저유동성 알트에서 부분체결/스프레드/슬리피지로 전략이 망가지는 것을 차단.

### 9.3 Strength 점수 (결정 고정)
BUY 후보에 대해:
- `strength = (abs(dev) * 0.5) + ((0.5 - %B) * 2.0) + ((30 - RSI) * 0.05)`
- DOWN_TREND 후보는 추가 가중치:
  - `strength += 1.0` (Macro Extreme 충족)
  - `strength += 1.0` (Divergence 충족)
  - `strength += 0.5` (Volume Capitulation 충족)

동점일 때 Tie-break:
1) 유동성 높은 마켓 우선(acc_trade_price_24h 큰 순)
2) 그 다음 알파벳(마켓 코드) 오름차순

---

## 10. 포지션 관리 / 추격매수 방지 (결정 고정)

### 10.1 DCA 금지
- 손실 상태에서 추가매수(DCA) **절대 금지**
- 피라미딩(수익 중 추가)도 기본 OFF:
  - `allowPyramiding = false`

### 10.2 StopLoss 이후 쿨다운
- StopLoss로 종료된 마켓은 재진입 제한:
  - UP_TREND/RANGE/TRANSITION: `cooldownBars = 12` (15m 기준 3시간)
  - DOWN_TREND: `downtrendCooldownMacroBars = 6` (4h 기준 24시간)

---

## 11. 리스크/청산 정책 (전략 권고 파라미터, 결정 고정)

> 실제 발동은 Risk Engine이 수행하되, EXTREME_FLIP은 레짐에 따라 권고값을 제공한다.

### 11.1 초기 손절 (ATR 기반)
- UP/RANGE/TRANSITION:
  - `SL = entry - 2.0 * ATR_15m`
- DOWN_TREND:
  - `SL = entry - 1.5 * ATR_15m` (칼날잡기 손실 제한)

### 11.2 익절/트레일링 (수익 극대화)
- UP_TREND:
  - TP1(부분청산 40%): `entry + 2.0 * ATR`
  - 나머지: 트레일링
    - trailingStart: `entry + 2.0 * ATR`
    - trail: `highest(close) - 2.0 * ATR`
- RANGE:
  - TP는 빠르게(평균회귀):
    - TP1(부분 50%): `BB_mid` 터치
    - TP2(잔량): Overbought Extreme + Flip Confirm(SELL 강화) 또는 trailing
- DOWN_TREND:
  - “반등만 먹고 빠지는” 빠른 청산:
    - TP1(부분 60%): `close >= EMA_fast` 또는 `entry + 1.2*ATR`
    - 잔량: trailing(짧게)
      - trailingStart: `entry + 1.2*ATR`
      - trail: `highest(close) - 1.5*ATR`
  - `maxHoldingBarsDown` 초과 시 시간청산(강제)

---

## 12. 주문 실행 정책 (운영 안정성 우선, 결정 고정)

EXTREME_FLIP의 주문 정책은 **“열릴 때는 깔끔하게(부분 체결/대기 최소화), 닫힐 때는 확실하게”**를 우선한다.

### 12.1 OrderAttempt / identifier 정책
- 모든 주문은 OrderAttempt 단위로 실행한다.
- Upbit 주문 생성의 `identifier`는 **항상 신규 생성**한다(재사용 금지).
  - Upbit 문서에서도 identifier는 유니크 값 사용을 요구한다. :contentReference[oaicite:1]{index=1}
- 네트워크 오류/타임아웃 시:
  - 동일 identifier로 재요청하지 않는다.
  - 주문 상태는 `UNKNOWN`으로 수렴시키고, `uuid 또는 identifier`로 조회하여 정합성을 회복한다. :contentReference[oaicite:2]{index=2}

### 12.2 진입(Entry) 주문 타입: **최유리 지정가(best) + FOK**
- BUY(매수):
  - `side=bid`, `ord_type=best`, `time_in_force=fok`
  - `price=KRW총액` 사용, `volume`은 제외/NULL
- SELL(매도) (진입은 매도가 없음)

최유리 주문과 time_in_force 제약은 Upbit 주문 문서에 명시되어 있다. :contentReference[oaicite:3]{index=3}

> FOK 채택 이유: 부분체결로 인해 “미세 포지션(dust)”이 생기면 이후 최소 주문 금액 제약에서 운영이 꼬인다. 진입은 “전량 체결 아니면 포기”가 안정적이다.

### 12.3 일반 익절(Profit-taking) 주문 타입: **최유리 지정가(best) + IOC**
- SELL(익절):
  - `side=ask`, `ord_type=best`, `time_in_force=ioc`
  - `volume=매도수량`, `price` 제외/NULL :contentReference[oaicite:4]{index=4}

> IOC 채택 이유: 익절은 “일부라도 체결 → 남은 건 다음 시도”가 가능하다. 대기 주문을 만들지 않는 것이 운영 안정성에 유리하다.

### 12.4 손절/비상청산(Emergency Exit) 주문 타입: **시장가 매도**
- SELL(손절/강제청산/418 등 비상상황):
  - `side=ask`, `ord_type=market`
  - `volume=전량`, `price` 제외/NULL :contentReference[oaicite:5]{index=5}

> 비상 상황에서 가격보다 “탈출 확실성”이 우선이다.

---

## 13. 상태 머신 (마켓별)

- `IDLE`
- `EXTREME_WATCH` (Extreme 감지, Flip 대기, TTL 보유)
- `IN_POSITION`
- `COOLDOWN`

전이:
- IDLE → EXTREME_WATCH: ExtremeDetected
- EXTREME_WATCH → IN_POSITION: FlipConfirmed + 레짐 게이트 통과 + Entry Fill
- EXTREME_WATCH → IDLE: TTL 만료 / 레짐 변경으로 무효화
- IN_POSITION → COOLDOWN: StopLoss / 시간청산 / 비상청산
- IN_POSITION → IDLE: 정상 익절 완료

---

## 14. Reason Codes (필수 로그 키)

- `EXTREME_DETECTED_OS`
- `EXTREME_DETECTED_OB`
- `FLIP_CONFIRMED_BUY`
- `FLIP_CONFIRMED_SELL_BIAS`
- `REGIME_BLOCKED_DOWN_TREND`
- `MACRO_EXTREME_MISSING`
- `DIVERGENCE_MISSING`
- `VOLUME_CAPITULATION_MISSING`
- `COOLDOWN_BLOCKED`
- `LIQUIDITY_BLOCKED`
- `ENTRY_ORDER_FOK_CANCELED`
- `EXIT_ORDER_PARTIAL_IOC`
- `STOPLOSS_EMERGENCY_MARKET_SELL`
- `TIME_EXIT_DOWN_TREND`

---

## 15. 백테스트 요구사항 (구현 고정)

- 신호 생성은 **종가 기준 확정 캔들**만 사용
- 멀티 TF 정렬: 15m candle at t에서 사용할 4h/1d는 **t 이전에 확정된 마지막 캔들**
- 체결 모델:
  - Entry: best+FOK → “해당 캔들 종가에 체결”로 단순화 가능(보수적이면 종가보다 불리하게 슬리피지 1틱 적용)
  - Profit exit: best+IOC → 부분체결 가능성을 모델링(단순 구현은 전량 체결로 시작, 이후 개선)
  - Emergency exit: market sell → 종가 - 슬리피지(ATR 비율) 적용

---

## 16. 검증 시나리오 (v1 실패 재현 포함)

### 16.1 v1 실패 재현(필수)
- 지속 하락 + 단기 반등 반복 구간 데이터
- 기대:
  - DOWN_TREND에서 거래 빈도 급감
  - MacroExtreme+Divergence 없이 진입하지 않음
  - 손절 후 24h 재진입 차단으로 추격매수 패턴 제거

### 16.2 횡보장
- 기대:
  - RANGE에서 평균회귀 거래
  - 승률/수익팩터 개선

### 16.3 강한 상승장
- 기대:
  - 고점 추격 없이 눌림에서만 진입
  - 트레일링으로 수익 구간 확대

---

## 17. 향후 확장(스펙 외)
- Extreme 임계값을 고정값이 아닌 rolling quantile 기반으로 동적화
- 시장 선택 자동화(유동성/변동성/상관성 기반)
- 슬리피지/스프레드 실측 기반 체결 품질 개선
- Partial fill/dust 처리 고도화(최소 주문 금액/호가단위 자동 보정)
