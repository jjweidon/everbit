# 성능/검증 계획 (백테스트 + 실거래 경로)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

이 문서는 “지표 산식/가정/목표치”를 고정한다.  
지표 정의가 흔들리면 백테스트 결과는 비교 불가능해진다.

---

## 1. 백테스트 지표 산식(고정)

### 1.1 기본 용어
- 초기 자본: `E0`
- 기간 종료 자본(Equity): `E1`
- 기간(연 단위): `Y` (예: 일수/365)

### 1.2 CAGR (연평균 성장률)
- `CAGR = (E1 / E0)^(1 / Y) - 1`

### 1.3 MDD (최대 낙폭)
- 자본곡선 `E(t)`에서
- `peak(t) = max_{u<=t} E(u)`
- `drawdown(t) = (E(t) - peak(t)) / peak(t)`
- `MDD = min_t drawdown(t)` (음수 값, 표시 시 절대값 %로 표현 가능)

### 1.4 승률(Win Rate)
- 트레이드 단위(P0 고정): “진입 1회 → 완전 청산 1회”를 1 트레이드로 정의
- `win_rate = wins / (wins + losses)`
- break-even(0)은 loss로 포함할지 여부를 고정해야 한다:
  - P0: 0은 loss로 처리(보수적)

### 1.5 수익 팩터(Profit Factor)
- `gross_profit = sum(trade_pnl where pnl > 0)`
- `gross_loss = abs(sum(trade_pnl where pnl < 0))`
- `profit_factor = gross_profit / gross_loss`
- `gross_loss = 0`인 경우:
  - P0: `profit_factor = +Infinity`로 기록하되, 비교에서는 별도 취급

---

## 2. 백테스트 가정(고정)

### 2.1 캔들 사용 규칙
- 신호는 **확정 캔들 close**에서만 생성한다(look-ahead 금지).
- 멀티 TF 정렬:
  - 15m 시점 t에서 사용하는 4h/1d 지표는 t 이전에 확정된 마지막 캔들 값이다.

### 2.2 체결 모델(초기 단순화)
P0에서는 “비교 가능성”을 우선한다.

- Entry/Exit 체결 가격은 기본적으로 “캔들 close” 기반으로 계산한다.
- 슬리피지/수수료는 파라미터로 주입한다.
  - `fee_rate`(0~1): 체결 금액 대비 수수료율
  - `slippage_bps`(basis point): 체결 가격에 가산/감산

기록 규칙:
- 백테스트 결과에는 사용된 `fee_rate`, `slippage_bps`, 주문 타입 모델을 반드시 저장한다.

---

## 3. 성능 목표치(초기)

### 3.1 API 응답(운영)
- 대시보드 조회(p95): 300ms 이내(로컬 VM 기준)
- 설정/킬스위치 변경(p95): 300ms 이내

### 3.2 주문 파이프라인
- 429 상황에서도 시스템은 다운되지 않고, 큐 적체로 degrade 되어야 한다.
- UNKNOWN 발생 시 자동 재주문을 하지 않고, reconcile/중단 정책이 동작해야 한다.

### 3.3 백테스트 처리량
- 스모크(필수): 2 markets × 2 TF × 30일은 CI에서 수분 내 완료
- 운영 목표(권장): 10 markets × 3 TF × 365일은 “현실적 시간” 내 완료
  - 정확한 SLA는 운영/데이터 크기 측정 후 ADR로 고정(P1)

---

## 4. 성능 테스트 계획

### 4.1 백테스트 벤치
- 입력 크기별로 실행시간/메모리/DB IO 기록
- 주요 병목:
  - 캔들 로딩 쿼리(인덱스)
  - 지표 계산(CPU)
  - 결과 저장(IO)

### 4.2 API 부하(k6/Gatling)
- 대시보드 조회(읽기) RPS 증가 테스트
- 설정 변경(쓰기) 낮은 빈도, 정합성 우선

### 4.3 장애/제한 시뮬레이션
- 429: THROTTLED 재시도(새 Attempt)
- 418: 차단 해제까지 호출 중단
- timeout/5xx: UNKNOWN 수렴 → reconcile → 실패 시 SUSPENDED

---

## 5. Done(검증 완료 조건)

- [ ] 지표 산식이 코드/문서/결과 저장에 일치
- [ ] 백테스트 결과가 동일 입력에서 재현 가능
- [ ] 429/418/UNKNOWN 시나리오를 테스트로 고정
