# client/Dockerfile

# 1단계
# 환경 설정 및 dependancy 설치

FROM node:20-alpine AS base

# 타임존 설정
RUN apk add --no-cache tzdata
ENV TZ=Asia/Seoul

FROM base AS deps
RUN apk add --no-cache libc6-compat

# 명령어를 실행할 디렉터리 지정
WORKDIR /app

# package.json만 먼저 복사
COPY package.json yarn.lock ./

# node_modules를 클린 설치
RUN rm -rf node_modules
RUN yarn install --frozen-lockfile

# 2단계 
FROM base AS builder 

WORKDIR /app

# node_modules를 먼저 복사
COPY --from=deps /app/node_modules ./node_modules

# 소스 파일 복사 (node_modules 제외)
COPY . .
RUN rm -rf node_modules/.cache

# Next.js 애플리케이션을 빌드
RUN yarn build

# 3단계
FROM base AS runner
WORKDIR /app

ENV NODE_ENV production

# container 환경에 시스템 사용자를 추가함
RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

# .next 디렉터리를 만들고 사용자 권한을 설정
RUN mkdir .next
RUN chown nextjs:nodejs .next

# 빌드 결과물 중 public 폴더를 복사
COPY --from=builder /app/public ./public
# standalone 결과물에는 public 폴더와 static 폴더 내용은 포함되지 않으므로, 따로 복사
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

# nextjs 사용자로 실행하도록 설정
USER nextjs

# 컨테이너의 수신 대기 포트를 3000으로 설정
EXPOSE 3000

# 포트 환경 변수 설정
ENV PORT 3000

# 로컬호스트 환경에서, node로 애플리케이션 실행
CMD HOSTNAME="0.0.0.0" node server.js