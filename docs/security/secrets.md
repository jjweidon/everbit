# Secrets & 민감정보 관리 규칙 (필수)

이 문서는 everbit v2에서 **절대 커밋하면 안 되는 값**과 **환경 분리(local/prod)**, **암호화/키관리** 원칙을 고정한다.  
전제: v2는 1인 전용 서비스이지만, **시크릿 유출은 즉시 실거래 피해**로 이어질 수 있으므로 “개인 프로젝트” 기준으로 완화하지 않는다.

---

## 1. 절대 Git에 커밋 금지 (Never Commit)

아래 항목은 **절대** Git에 커밋하지 않는다.

### 1) 인증/로그인
- Kakao OAuth Client Secret
- OAuth redirect URI 중 내부망/테스트용 값(실수 방지 목적)

### 2) Upbit
- Upbit Access Key
- Upbit Secret Key
- Upbit API 관련 JWT 서명키/토큰(생성되는 경우)

### 3) 서버 보안
- JWT 서명키(Access/Refresh)
- AES 마스터키(Upbit 키 암호화용)
- 세션/쿠키 암호화 키
- 관리자 페이지/운영 토큰(있다면)

### 4) 인프라/데이터베이스
- PostgreSQL 사용자/패스워드/접속 문자열(운영)
- Redis 패스워드/ACL(운영)
- Kafka SASL/SSL 설정(도입 시)
- Grafana admin password
- Jenkins admin password / API token
- TLS 개인키(.pem/.p12/.jks 등)

### 5) 데이터/로그
- 실거래 주문/체결/잔고 내역이 포함된 원본 로그
- 백테스트 원본 데이터(저작권/약관/보안 이슈 가능)

---

## 2. 파일/폴더 관리 규칙

### 2.1 `.gitignore` (필수)
- `.env*`, `application*.yml|yaml`, 인증서 파일, 로그, 빌드 산출물은 무조건 ignore.
- **주의:** `docs/`는 커밋 대상이다. 문서 우선 개발이기 때문에 `/docs/`를 ignore 하면 안 된다.  
  (필요하면 `docs/export/`, `docs/_build/`만 ignore)

### 2.2 `.cursorignore` (필수)
- Cursor(또는 유사 AI 인덱싱)가 **시크릿/대용량 데이터/로그**를 읽지 않도록 `.cursorignore`를 반드시 유지한다.
- 새로 생긴 민감 경로는 즉시 `.cursorignore`에 추가한다.

---

## 3. 환경 분리 원칙 (local / prod)

### 3.1 Local
- 파일: `.env.local` (커밋 금지)
- 목적: 개발 편의. 단, 로컬에서만 사용.

### 3.2 Prod
- 원칙: **VM 환경변수로 주입** (파일 기반 설정 지양)
- docker compose를 쓰더라도 운영에서는:
  - `docker compose --env-file`를 쓰되 **서버 로컬에만 저장**하고 Git에 절대 포함하지 않는다.
  - 가능하면 systemd unit 또는 shell profile로 환경변수 주입.

---

## 4. Upbit API 키 저장 정책 (필수)

### 4.1 저장은 하되, “암호문”만 DB에 저장
- DB에 평문 저장 금지.
- 저장 형태 예시:
  - `upbit_access_key_enc`
  - `upbit_secret_key_enc`
  - `upbit_key_version` (키 롤오버 대비)
  - `created_at`, `rotated_at`

### 4.2 암호화 알고리즘/키관리
- 권장: **AES-256-GCM** (인증 암호화)
- `MASTER_KEY`(32 bytes)는 운영 VM 환경변수로만 주입.
- IV/nonce는 매 암호화마다 랜덤 생성, 암호문과 함께 저장.
- AAD(추가 인증 데이터)는 사용자 id(OWNER) + key_version 등을 사용해도 됨.

### 4.3 키 로테이션(최소 규칙)
- `MASTER_KEY`가 교체될 수 있으므로 `key_version`을 둔다.
- 로테이션 시:
  1) 신규 MASTER_KEY로 재암호화
  2) 기존 키는 즉시 폐기(운영 환경변수에서 제거)

---

## 5. 로그/관측 데이터 취급

- 로그에 Access Key/Secret Key/JWT/쿠키 값이 찍히지 않도록 마스킹 처리.
- 에러 로그에 Upbit 요청/응답 raw body 전체를 남기지 않는다.
- Prometheus 메트릭에 **식별 가능한 개인정보/시크릿** 포함 금지.

---

## 6. 운영 점검 체크리스트 (릴리즈 전)

- [ ] `.env*`, `application*.yml|yaml`가 레포에 포함되지 않았는지 확인
- [ ] `.cursorignore` 최신화
- [ ] Upbit 키는 DB에 암호문으로만 존재
- [ ] JWT/AES 마스터키가 VM 환경변수에만 존재
- [ ] Grafana/Jenkins 기본 비밀번호 변경
- [ ] 서버 방화벽: 불필요 포트 외부 차단(특히 DB/Redis/Kafka)
