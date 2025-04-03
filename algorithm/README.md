# Everbit 알고리즘 모듈

이 모듈은 Everbit 프로젝트의 트레이딩 알고리즘 및 백테스팅 기능을 제공합니다.

## 주요 기능

- 실시간 비트코인 시세 수집 및 분석
- 퀀트 알고리즘 기반 트레이딩 전략
- 백테스팅 모듈
- 실시간 트레이딩 모듈
- Upbit API 연동

## 프로젝트 구조

```
algorithm/
├── src/
│   ├── api/              # FastAPI 기반 API 서버
│   ├── backtest/         # 백테스팅 모듈
│   ├── data/             # 데이터 수집 및 처리
│   ├── models/           # ML/DL 모델
│   ├── strategies/       # 트레이딩 전략
│   ├── utils/            # 유틸리티 함수
│   └── main.py           # 메인 엔트리 포인트
├── data/                 # 저장된 데이터
├── tests/                # 단위 테스트
├── pyproject.toml        # 프로젝트 설정
└── requirements.txt      # 의존성 패키지
```

## 설치 및 실행

```bash
# 의존성 설치
pip install -r requirements.txt

# 환경 변수 설정
cp .env.example .env
# .env 파일 수정

# 백테스팅 실행
python -m src.backtest

# API 서버 실행
python -m src.api.server

# 트레이딩 봇 실행
python -m src.main
```

## 사용 가능한 전략

### 이동평균선(MA) 크로스오버
- 단기 이동평균선이 장기 이동평균선을 상향 돌파하면 매수
- 단기 이동평균선이 장기 이동평균선을 하향 돌파하면 매도
- 기본 파라미터: 단기(10일), 장기(30일)

### 볼린저 밴드 + RSI 조합 전략
- 매수 시점: 
  - RSI가 30 이하로 과매도 상태에 진입하고
  - 가격이 볼린저 밴드 하단에 근접하거나 돌파했을 때
  - MACD 히스토그램이 상승 반전 신호를 보일 때
- 매도 시점:
  - RSI가 70 이상으로 과매수 상태에 진입하고
  - 가격이 볼린저 밴드 상단에 근접하거나 돌파했을 때
  - 또는 볼린저 밴드 폭이 감소하기 시작할 때
- 손절 및 이익실현:
  - 손절: 매수가 대비 5% 하락 시 손절
  - 이익실현: 
    - 첫 번째 목표: 매수가 대비 10% 상승 시 포지션의 50% 매도
    - 두 번째 목표: 매수가 대비 20% 상승 시 나머지 50% 매도
- 자금 관리: 전체 투자 자금의 30%만 사용

### 향후 추가 예정 전략
- RSI 기반 과매수/과매도
- MACD 시그널
- 볼린저 밴드 전략
- 모멘텀 전략
- 평균회귀 전략
- 머신러닝/딥러닝 기반 예측 모델

## 백테스팅

백테스팅은 Backtrader 또는 Zipline 라이브러리를 사용하여 수행됩니다. 과거 데이터를 사용하여 전략의 성능을 테스트할 수 있습니다.

```bash
# 백테스팅 실행 예시
python -m src.backtest --strategy ma_crossover --start 2022-01-01 --end 2023-01-01

# 볼린저 밴드 + RSI 전략 백테스팅
python -m src.backtest --strategy bollinger_rsi --start 2022-01-01 --end 2023-01-01
```

## 개발 환경

```bash
# 테스트 실행
pytest

# 코드 포맷팅
black .
isort .
```
