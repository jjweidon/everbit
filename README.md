# 에버비트 (Everbit)

비트코인 자동 트레이딩 시스템으로, Upbit API를 기반으로 퀀트 전략을 활용하여 최적의 매매 타이밍을 자동으로 판단하고 실행합니다.

## 프로젝트 구조

- `algorithm/`: Python 기반 퀀트 알고리즘 및 백테스팅 모듈
- `server/`: Spring Boot 기반 백엔드 API 서버
- `client/`: Next.js 기반 프론트엔드
- `common/`: 공통 유틸리티 및 라이브러리
- `docs/`: 프로젝트 문서
- `shared/`: 백엔드와 프론트엔드 간 공유 타입 및 상수

## 주요 기능

- 실시간 비트코인 시세 수집 및 분석
- 퀀트 알고리즘 기반 자동 매매
- 백테스팅 기능 제공
- 포트폴리오 및 리스크 관리
- 트레이딩 성과 대시보드 제공

## 개발 환경 설정

### 요구 사항

- JDK 17 이상
- Python 3.10 이상
- Node.js 18 이상
- Docker

### 설치 및 실행

```bash
# 백엔드 실행
cd server
./gradlew bootRun

# 알고리즘 모듈 설정
cd algorithm
pip install -r requirements.txt

# 프론트엔드 실행
cd client
npm install
npm run dev
```

## 기술 스택

- **백엔드**: Spring Boot 3.x, WebFlux, Kafka, PostgreSQL, TimescaleDB, Redis
- **알고리즘**: Python, TensorFlow, scikit-learn, PyTorch, Backtrader, Zipline
- **프론트엔드**: Next.js, TypeScript, TailwindCSS, Chakra UI, TradingView API, SWR
- **인프라**: Docker, Github Actions, AWS
