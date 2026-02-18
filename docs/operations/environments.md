# 환경 분리 및 설정 표준 (local / prod)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

원칙:
- 시크릿은 절대 Git에 커밋하지 않는다.
- 로컬: `.env.local`
- 운영: VM 환경변수 또는 root-only env 파일(`/etc/everbit/everbit.env`, 600)
- Docker Compose는 local/prod로 “포트 노출/보안”을 다르게 가져간다.

v2 MVP에서는 Kafka를 사용하지 않는다. 비동기 파이프라인은 PostgreSQL Outbox/Queue(`outbox_event`) + 워커 폴링으로 구현한다.

---

## 1. 권장 파일 구조

```text
/
  docker/
    compose.yaml
    compose.local.yaml
    compose.prod.yaml
    nginx/conf.d/default.conf
    prometheus/prometheus.yml
    grafana/provisioning/...
  server/
  client/
  .env.example
  .env.local        # 로컬 전용 (gitignore)
```

---

## 2. Spring Profile
- `SPRING_PROFILES_ACTIVE=local|prod`

---

## 3. 환경변수 표준

### 3.1 공통
- `TZ=Asia/Seoul`

### 3.2 Server(Spring Boot)
- `SERVER_PORT=8080`

#### Auth/JWT
- `JWT_ACCESS_SECRET=...`
- `JWT_REFRESH_SECRET=...`
- `JWT_ACCESS_TTL_SECONDS=900` (예: 15m)
- `JWT_REFRESH_TTL_SECONDS=1209600` (예: 14d)

#### OAuth(Kakao)
- `KAKAO_CLIENT_ID=...`
- `KAKAO_CLIENT_SECRET=...`
- `KAKAO_REDIRECT_URI=https://api.everbit.kr/oauth2/callback/kakao`

#### Upbit(암호화)
- `UPBIT_KEY_MASTER_KEY=...`  # 32 bytes, 운영 시크릿

#### Web Push
- `VAPID_SUBJECT=mailto:admin@everbit.kr`
- `VAPID_PUBLIC_KEY=...`
- `VAPID_PRIVATE_KEY=...`     # 운영 시크릿

### 3.3 Database(PostgreSQL)
- `POSTGRES_HOST=postgres`
- `POSTGRES_PORT=5432`
- `POSTGRES_DB=everbit`
- `POSTGRES_USER=everbit`
- `POSTGRES_PASSWORD=...`

### 3.4 Redis
- `REDIS_HOST=redis`
- `REDIS_PORT=6379`
- `REDIS_PASSWORD=` (초기 미사용, 운영에서 필요 시 적용)

### 3.5 Outbox/Queue Worker(권장)
> v2 MVP에서 Kafka를 대체하는 핵심 구성이다.

권장(선택) 튜닝 변수:
- `OUTBOX_WORKER_ENABLED=true` (기본 true)
- `OUTBOX_WORKER_POLL_MS=200` (폴링 간격)
- `OUTBOX_WORKER_BATCH_SIZE=50`
- `OUTBOX_WORKER_LOCK_TTL_SECONDS=30`
- `OUTBOX_WORKER_MAX_ATTEMPTS=10`

### 3.6 Observability(Actuator)
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus`
- `MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true`

### 3.7 Client(Next.js / Vercel)
- `NEXT_PUBLIC_API_BASE_URL=https://api.everbit.kr`
- `NEXT_PUBLIC_VAPID_PUBLIC_KEY=...`  # 푸시 구독 생성에 필요(공개키)

---

## 4. .env 파일 정책

### 4.1 커밋 가능한 파일
- `.env.example` : 키 이름만 제공(값 비움)

### 4.2 커밋 금지 파일
- `.env.local`
- `.env.prod`
- 시크릿 포함 `application-*.yml` 등

---

## 5. Docker Compose 분리 전략

### 5.1 공통 `docker/compose.yaml`
- postgres, redis, prometheus, grafana, server, nginx (jenkins는 선택)
- 공통 네트워크/볼륨 정의

### 5.2 로컬 오버라이드 `docker/compose.local.yaml`
- 개발 편의를 위해 필요한 포트 노출 허용(예: postgres:5432)
- `SPRING_PROFILES_ACTIVE=local`

### 5.3 운영 오버라이드 `docker/compose.prod.yaml`
- 외부 노출은 nginx(80/443)만 허용
- Admin 도구는 **127.0.0.1에만 publish** 또는 publish 금지
  - 표준 권장: `127.0.0.1:3000:3000`(Grafana), `127.0.0.1:9090:9090`(Prometheus), `127.0.0.1:8081:8081`(Jenkins)
- `SPRING_PROFILES_ACTIVE=prod`

실행 예시:
```bash
# local
docker compose -f docker/compose.yaml -f docker/compose.local.yaml up -d

# prod
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml up -d
```

---

## 6. 운영 환경변수 주입(표준)

### 6.1 방법 A: env_file(권장)
- `/etc/everbit/everbit.env` 생성(root)
- 권한: `chmod 600 /etc/everbit/everbit.env`
- compose에 `env_file: [/etc/everbit/everbit.env]`

### 6.2 방법 B: systemd(대안)
- 변경 이력/운영 난이도가 증가하므로 A가 기본

---

## 7. 운영 체크리스트

- [ ] `.gitignore`에서 `/docs/` ignore 제거 완료
- [ ] `.env.local` / `.env.prod` gitignore 등록 완료
- [ ] 운영 VM에 `/etc/everbit/everbit.env` 생성 + 권한 600 적용
- [ ] nginx에서 `/actuator` 접근 차단 또는 allowlist 적용
- [ ] Admin 도구 포트 외부 노출 없음(OCI/iptables/docker publish 모두)
