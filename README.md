## 🚀 프로젝트 소개

everbit는 업비트 거래소의 API를 활용한 자동화된 암호화폐 트레이딩 시스템입니다. 다양한 퀀트 전략을 통해 시장 데이터를 분석하고, 최적의 매매 시점을 자동으로 판단하여 거래를 실행합니다.

## ✨ 주요 기능

- 🔄 실시간 비트코인 시세 수집 및 분석
- 🤖 퀀트 알고리즘 기반 자동 매매
- 📊 백테스팅 기능 제공
- 💼 포트폴리오 및 리스크 관리
- 📈 트레이딩 성과 대시보드 제공
- 🔐 OAuth2 소셜 로그인 (카카오)
- 📱 반응형 웹 인터페이스

## 🛠 기술 스택

### 백엔드
- **Framework**: Spring Boot
- **Language**: Java
- **Database**: MySQL, Redis
- **API**: Upbit API
- **Security**: OAuth2, JWT

### 프론트엔드
- **Framework**: Next.js
- **Language**: TypeScript
- **Styling**: TailwindCSS
- **Chart**: TradingView API

### 인프라
- **Container**: Docker
- **Web Server**: Nginx
- **Cloud**: AWS EC2

## 📖 사용법

### 1. 회원가입 및 로그인
- 카카오 소셜 로그인을 통해 서비스 이용

### 2. Upbit API 키 등록
- 마이페이지에서 Upbit 계정의 API 키 등록
- 거래 권한이 있는 API 키 필요

### 3. 트레이딩 봇 설정
- 대시보드에서 트레이딩 전략 선택
- 거래할 마켓 선택
- 최소/최대 주문 금액 설정

### 4. 모니터링
- 실시간 포트폴리오 현황 확인
- 거래 내역 및 수익률 추적


## 🐳 도커 명령어
```bash
# 프로덕션 환경 실행
docker compose -f docker-compose.yaml up -d --build --force-recreate

# 개발 환경 실행
docker compose -f docker-compose.dev.yaml up -d --build --force-recreate
```

```bash
# 프로덕션 환경 종료
docker compose -f docker-compose.yaml down

# 개발 환경 종료
docker compose -f docker-compose.dev.yaml down
```

```bash
# 서버 로그 실시간 확인
docker compose logs -f server
```
