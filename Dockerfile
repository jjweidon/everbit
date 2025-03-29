### 1. Server Build Stage ###
FROM gradle:8.5-jdk17 AS server-build
WORKDIR /app/server
COPY server .
RUN ./gradlew build -x test

### 2. Client Build Stage ###
FROM node:18-alpine AS client-build
WORKDIR /app/client
COPY client/package*.json ./
RUN npm ci
COPY client .
RUN npm run build

### 3. Algorithm Build Stage ###
FROM python:3.10-slim AS algorithm-build
WORKDIR /app/algorithm
COPY algorithm/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY algorithm .

### 4. Final Runtime Stage ###
FROM openjdk:17-slim
WORKDIR /app

# 서버 JAR 파일 복사
COPY --from=server-build /app/server/build/libs/*.jar ./server.jar

# 클라이언트 빌드 결과 복사
COPY --from=client-build /app/client/.next ./client/.next
COPY --from=client-build /app/client/public ./client/public
COPY --from=client-build /app/client/package*.json ./client/
COPY --from=client-build /app/client/next.config.js ./client/

# 알고리즘 코드 복사
COPY --from=algorithm-build /app/algorithm ./algorithm

# Python 설치
RUN apt-get update && apt-get install -y python3 python3-pip && \
    pip3 install --no-cache-dir -r algorithm/requirements.txt && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Node.js 설치
RUN apt-get update && apt-get install -y curl && \
    curl -fsSL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 포트 노출
EXPOSE 8080 3000

# 스크립트 복사 및 실행 권한 부여
COPY docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"] 