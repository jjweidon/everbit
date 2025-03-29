#!/bin/bash
set -e

# 환경 변수 설정 확인
if [ -z "$UPBIT_ACCESS_KEY" ]; then
  echo "Warning: UPBIT_ACCESS_KEY is not set. Using dummy value for development."
  export UPBIT_ACCESS_KEY="dummy_access_key"
fi

if [ -z "$UPBIT_SECRET_KEY" ]; then
  echo "Warning: UPBIT_SECRET_KEY is not set. Using dummy value for development."
  export UPBIT_SECRET_KEY="dummy_secret_key"
fi

# 서비스 실행 함수
start_server() {
  echo "Starting Spring Boot server..."
  java -jar /app/server.jar &
  SERVER_PID=$!
}

start_client() {
  echo "Starting Next.js client..."
  cd /app/client
  npm install --production
  npm start &
  CLIENT_PID=$!
}

start_algorithm() {
  echo "Starting Python algorithm service..."
  cd /app/algorithm
  python -m src.main --backtest &
  ALGORITHM_PID=$!
}

# 모든 서비스 종료 함수
cleanup() {
  echo "Shutting down services..."
  [ -n "$SERVER_PID" ] && kill $SERVER_PID
  [ -n "$CLIENT_PID" ] && kill $CLIENT_PID
  [ -n "$ALGORITHM_PID" ] && kill $ALGORITHM_PID
  exit 0
}

# 종료 시그널 처리
trap cleanup SIGTERM SIGINT

# 서비스 실행
case "$1" in
  server)
    start_server
    ;;
  client)
    start_client
    ;;
  algorithm)
    start_algorithm
    ;;
  all|"")
    start_server
    start_client
    start_algorithm
    ;;
  *)
    echo "Unknown service: $1"
    echo "Usage: $0 {server|client|algorithm|all}"
    exit 1
    ;;
esac

# 모든 백그라운드 프로세스가 종료될 때까지 대기
wait 