# 환경 분리 및 설정 표준 (local / prod)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

원칙:
- 시크릿은 절대 Git에 커밋하지 않는다.
- 로컬: `.env.local`
- 운영: VM 환경변수 또는 root-only env 파일(`/etc/everbit/everbit.env`, 600)
- Docker Compose는 local/prod로 “포트 노출/보안”을 다르게 가져간다.
- **운영(prod) DB/Auth/Storage는 Supabase**를 사용하며, **VM에는 DB 컨테이너를 띄우지 않는다.**

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

### 3.3 Database(PostgreSQL) — 운영은 Supabase

**운영(prod)**  
DB는 **Supabase(Postgres)**를 사용한다. VM에 Postgres 컨테이너를 띄우지 않는다.

예시 키(placeholder만 사용, 실제 값 커밋 금지):

```bash
# Supabase Session pooler (권장: Session mode, 포트 5432)
SPRING_DATASOURCE_URL=jdbc:postgresql://aws-0-<region>.pooler.supabase.com:5432/postgres?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres.<project-ref>
SPRING_DATASOURCE_PASSWORD=<DB_PASSWORD>

# Supabase API (Auth/Storage 등)
SUPABASE_URL=https://<project-ref>.supabase.co
SUPABASE_ANON_KEY=<ANON_KEY>
SUPABASE_SERVICE_ROLE_KEY=<SERVICE_ROLE_KEY>
```

주의:
- **SERVICE_ROLE_KEY**는 서버 전용이다. 프론트엔 절대 두지 않는다. 프론트는 `ANON_KEY`만 사용한다.
- 연결 풀은 E2.1.Micro에 맞게 작게 유지한다(`docs/architecture/spring-boot-conventions.md` 또는 Supabase 가이드 참고).

**로컬(개발용)**  
로컬에서만 Postgres/Redis를 컨테이너로 띄울 수 있다. 이때는 아래와 같이 호스트명을 사용한다.

```bash
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
POSTGRES_DB=everbit
POSTGRES_USER=everbit
POSTGRES_PASSWORD=<로컬 전용 비밀번호>
```

또는 로컬에서도 Supabase 연결 문자열을 사용할 수 있다(팀 정책에 따름).

### 3.4 Redis(로컬/캐시)
- **운영**: VM에 Redis 컨테이너를 띄우지 않는다. 캐시/세션은 Supabase 또는 애플리케이션 메모리로 처리한다(구현에 따라 상이).
- **로컬**: 개발용으로만 Redis 사용 시
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
- `NEXT_PUBLIC_API_BASE=https://api.everbit.kr`  # 운영 API URL (미설정 시 localhost 사용 → Vercel 배포 시 무한 요청)
- `NEXT_PUBLIC_API_BASE_URL`  # 호환용 (동일 용도, NEXT_PUBLIC_API_BASE 우선)
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
- 서비스: server, (로컬 오버라이드에서만 postgres, redis), prometheus, grafana, nginx (jenkins는 선택)
- **운영(prod)에서는 postgres/redis 서비스를 포함하지 않는다.** DB는 Supabase에 연결한다.

### 5.2 로컬 오버라이드 `docker/compose.local.yaml`
- **개발용 only**: postgres, redis 등 필요한 포트 노출
- `SPRING_PROFILES_ACTIVE=local`

### 5.3 운영 오버라이드 `docker/compose.prod.yaml`
- **DB/Redis 서비스 없음.** Backend는 `SPRING_DATASOURCE_*` 등으로 Supabase 연결.
- Backend 서비스에 **JVM 메모리 상한** 적용: `JAVA_TOOL_OPTIONS=-Xms128m -Xmx256m -XX:MaxMetaspaceSize=96m`
- `env_file: [/etc/everbit/everbit.env]` 사용. repo 내부 `.env` 금지.
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
