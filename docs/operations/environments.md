# 환경 분리 및 설정 표준 (local / prod)

## 1. 목표

Everbit v2는 다음 원칙으로 환경을 분리한다.

- **시크릿은 절대 Git에 커밋하지 않는다**
- 로컬: `.env.local`
- 운영: **VM 환경변수** 또는 root-only env 파일(`/etc/everbit/everbit.env`)
- Docker Compose는 local/prod를 분리하여 “노출 포트/리소스/보안”을 다르게 가져간다.

---

## 2. 권장 파일 구조

```text
/
  docker/
    compose.yaml
    compose.local.yaml
    compose.prod.yaml
    nginx/
      conf.d/default.conf
    prometheus/
      prometheus.yml
    grafana/
      provisioning/...
  server/
  client/
  .env.example
  .env.local        # 로컬 전용 (gitignore)
```

---

## 3. Spring Profile 표준

- `local`: 개발 편의(디버그 로그, Swagger on 등)
- `prod`: 운영(보안 헤더, actuator 제한, 로그 레벨 제한)

환경변수:
- `SPRING_PROFILES_ACTIVE=local|prod`

---

## 4. 환경변수 표준

### 4.1 공통
- `TZ=Asia/Seoul`

### 4.2 Server (Spring Boot)
- `SERVER_PORT=8080`
- `JWT_SECRET=...`
- `JWT_REFRESH_SECRET=...` (또는 refresh 저장 전략에 따라 생략)
- `UPBIT_KEY_MASTER_KEY=...`  # 업비트 키 암호화 마스터키(AES-GCM)
- `KAKAO_CLIENT_ID=...`
- `KAKAO_CLIENT_SECRET=...`
- `KAKAO_REDIRECT_URI=https://api.everbit.kr/oauth2/callback/kakao`

### 4.3 Database (PostgreSQL)
- `POSTGRES_HOST=postgres`
- `POSTGRES_PORT=5432`
- `POSTGRES_DB=everbit`
- `POSTGRES_USER=everbit`
- `POSTGRES_PASSWORD=...`

### 4.4 Redis
- `REDIS_HOST=redis`
- `REDIS_PORT=6379`
- `REDIS_PASSWORD=` (초기엔 미사용, 운영에서 필요 시 적용)

### 4.5 Kafka
- `KAFKA_BOOTSTRAP_SERVERS=kafka:9092`

### 4.6 Observability
- `MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,prometheus`
- `MANAGEMENT_ENDPOINT_HEALTH_PROBES_ENABLED=true`

### 4.7 Client (Next.js)
- `NEXT_PUBLIC_API_BASE_URL=https://api.everbit.kr`

---

## 5. .env 파일 정책

### 5.1 커밋 가능한 파일
- `.env.example` : 키 이름만 제공(값은 비움)

### 5.2 커밋 금지 파일
- `.env.local`
- `.env.prod`
- `**/application-prod.yml` 등 시크릿 포함 파일

---

## 6. Docker Compose 분리 전략

### 6.1 기본(공통) `docker/compose.yaml`
- postgres, redis, kafka, prometheus, grafana, server, nginx, jenkins 정의
- 공통 네트워크/볼륨 정의

### 6.2 로컬 오버라이드 `docker/compose.local.yaml`
- 포트 노출(예: postgres:5432, kafka:9092 등) 허용
- 개발 편의 도구 on
- `SPRING_PROFILES_ACTIVE=local`

### 6.3 운영 오버라이드 `docker/compose.prod.yaml`
- DB/Redis/Kafka 포트 외부 노출 금지
- nginx만 80/443 노출
- `SPRING_PROFILES_ACTIVE=prod`
- 리소스 제한(가능하면): cpu/mem limit 설정
- Grafana/Jenkins 외부 노출 금지 또는 allowlist

실행 예시:

```bash
# local
docker compose -f docker/compose.yaml -f docker/compose.local.yaml up -d

# prod
docker compose -f docker/compose.yaml -f docker/compose.prod.yaml up -d
```

---

## 7. 운영 환경변수 주입 표준

### 7.1 방법 A: docker compose env_file (권장)
- `/etc/everbit/everbit.env` 생성(root 권한)
- 권한: 600
- docker compose에 `env_file: [/etc/everbit/everbit.env]`로 주입

### 7.2 방법 B: systemd 환경변수(대안)
- `systemctl edit docker` 또는 서비스 유닛 Drop-in으로 설정
- 운영 난이도 증가 가능(변경 이력 관리 필요)

---

## 8. 보안 필수 규칙

- 운영 VM에는 **.env 파일을 repo 폴더 내부에 두지 않는다**
  - 실수로 `git add`할 위험 제거
- Jenkins를 운영 VM에 둘 경우:
  - **기본값: 외부 비공개 + SSH 터널로만 접근** (ADR 0006)
  - 예외적으로 공개가 필요하면 Basic auth + IP allowlist 적용
  - Credentials는 Jenkins Credentials Store에 저장(커밋 금지)

---

## 9. 체크리스트

- [ ] `.gitignore`에서 `/docs/` ignore 제거 완료
- [ ] `.env.local` / `.env.prod` gitignore 등록 완료
- [ ] `.cursorignore`에 시크릿/로그/데이터/산출물 등록 완료
- [ ] 운영 VM에 `/etc/everbit/everbit.env` 생성 및 권한 600 적용
- [ ] nginx에서 `/actuator` 접근 제한(IP allowlist)
