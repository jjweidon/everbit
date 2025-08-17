## 🚀 프로젝트 소개 | Introduction

everbit는 업비트 거래소의 API를 활용한 자동화된 암호화폐 트레이딩 시스템입니다. 다양한 퀀트 전략을 통해 시장 데이터를 분석하고, 최적의 매매 시점을 자동으로 판단하여 거래를 실행합니다.

Everbit is an automated cryptocurrency trading system that leverages the Upbit exchange API. It analyzes market data through various quantitative strategies and automatically executes trades by determining optimal entry and exit points.

## ✨ 주요 기능 | Key Features

- 🔄 **실시간 마켓 시세 수집 및 분석**  
  Real-time market data collection and analysis
- 🤖 **퀀트 알고리즘 기반 자동 매매**  
  Quantitative algorithm-based automated trading
    - **RSI** (Relative Strength Index) - 상대강도지수를 활용한 과매수/과매도 신호 분석
    - **BB** (Bollinger Bands) - 볼린저 밴드를 통한 변동성 기반 매매 신호
    - **MACD** (Moving Average Convergence Divergence) - 이동평균 수렴확산을 이용한 추세 분석
    - **EMA** (Exponential Moving Average) - 지수이동평균을 활용한 단기 추세 파악
- 📊 **백테스팅 기능 제공**  
  Backtesting functionality for strategy validation
- 💼 **포트폴리오 및 리스크 관리**  
  Portfolio and risk management system
- 📈 **트레이딩 성과 대시보드 제공**  
  Trading performance dashboard with analytics
- 🔐 **OAuth2 소셜 로그인 (카카오)**  
  OAuth2 social login integration (Kakao)
- 📱 **반응형 웹 인터페이스**  
  Responsive web interface for all devices

## 🛠 기술 스택 | Tech Stack

### 백엔드 | Backend
- **Framework**: Spring Boot
- **Language**: Java
- **Database**: MySQL, Redis
- **API**: Upbit API
- **Security**: OAuth2, JWT

### 프론트엔드 | Frontend
- **Framework**: Next.js
- **Language**: TypeScript
- **Styling**: TailwindCSS
- **Chart**: TradingView API

### 인프라 | Infrastructure
- **Container**: Docker
- **Web Server**: Nginx
- **Cloud**: AWS EC2

## 📖 사용법 | Usage

### 1. 회원가입 및 로그인 | Sign Up & Login
- 카카오 소셜 로그인을 통해 서비스 이용

### 2. Upbit API 키 등록 | Upbit API Key Registration
- 마이페이지에서 Upbit 계정의 API 키 등록
- 거래 권한이 있는 API 키 필요

### 3. 트레이딩 봇 설정 | Trading Bot Configuration
- 대시보드에서 트레이딩 전략 선택
- 거래할 마켓 선택
- 최소/최대 주문 금액 설정

### 4. 모니터링 | Monitoring
- 실시간 포트폴리오 현황 확인
- 거래 내역 및 수익률 추적


## 🐳 도커 명령어 | Docker Commands
```bash
# 프로덕션 환경 실행
docker compose -f docker-compose.yaml up -d --build --force-recreate

# 개발 환경 실행 (Private)
docker compose -f docker-compose.dev.yaml up -d --build --force-recreate
```

```bash
# 프로덕션 환경 종료
docker compose -f docker-compose.yaml down

# 개발 환경 종료 (Private)
docker compose -f docker-compose.dev.yaml down
```

```bash
# 서버 로그 실시간 확인
docker compose logs -f server
```
