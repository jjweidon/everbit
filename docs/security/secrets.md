# Secrets & 민감정보 관리 규칙 (필수)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

이 문서는 everbit v2에서 **절대 커밋하면 안 되는 값**과 **환경 분리(local/prod)**, **암호화/키관리** 원칙을 고정한다.  
1인 프로젝트라도 실거래 피해가 발생할 수 있으므로 보안을 완화하지 않는다.

---

## 1. 절대 Git에 커밋 금지(Never Commit)

아래 항목은 **절대** Git에 커밋하지 않는다.

### 1.1 인증/로그인
- `KAKAO_CLIENT_SECRET`
- 인증 관련 임시 토큰/콜백 테스트 값

### 1.2 Upbit
- Upbit Access Key / Secret Key(평문)
- Upbit 키 암호문 덤프(백업 파일에 포함되는 경우)

### 1.3 서버 보안
- `JWT_ACCESS_SECRET`, `JWT_REFRESH_SECRET`
- Refresh 세션 서명/암호화 키(도입 시)
- **Upbit 키 암호화 마스터키**: `UPBIT_KEY_MASTER_KEY` (운영 시크릿)
- 운영 관리자 토큰(있다면)

### 1.4 푸시 알림(Web Push)
- `VAPID_PRIVATE_KEY` (운영 시크릿)
- (선택) VAPID 공개키는 공개 가능하지만, 운영에서는 함께 관리해도 된다.

### 1.5 인프라/DB
- PostgreSQL/Redis 운영 비밀번호/토큰 (Kafka는 P1+ 도입 시 별도 관리)
- Grafana admin password
- Jenkins admin password / API token
- TLS 개인키(.pem/.p12/.jks)

### 1.6 데이터/로그
- 실거래 주문/체결/잔고가 포함된 원본 로그(개인 식별/자산 노출)
- 백테스트 원본 데이터(약관/보안 이슈 가능)

---

## 2. 파일/폴더 규칙

### 2.1 `.gitignore`
- `.env*`, `application*.yml|yaml`, 인증서 파일, 로그, 빌드 산출물은 무조건 ignore.
- **주의**: `docs/`는 커밋 대상이다. `/docs/`를 ignore 하지 않는다.

### 2.2 `.cursorignore`
- 시크릿/대용량 데이터/로그/산출물을 AI 인덱싱에서 제외한다.
- 새 민감 경로가 생기면 즉시 `.cursorignore`에 추가한다.

---

## 3. 환경 분리(local/prod)

### 3.1 Local
- `.env.local` (커밋 금지)
- 개발 편의를 위한 값만 둔다.

### 3.2 Prod
- 원칙: VM 환경변수로 주입
- 대안: root-only env 파일(`/etc/everbit/everbit.env`, 권한 600)
- 운영 서버에서 repo 폴더 내부에 `.env` 파일을 두지 않는다(실수로 add 방지).

---

## 4. Upbit 키 저장 정책(필수)

### 4.1 DB에는 암호문만 저장
- 평문 저장 금지
- 저장 컬럼 예:
  - `access_key_enc`, `secret_key_enc`, `key_version`

### 4.2 암호화/키관리
- 알고리즘: AES-256-GCM(인증 암호)
- 마스터키: `UPBIT_KEY_MASTER_KEY`(32 bytes)
  - 로컬: `.env.local`에서 주입
  - 운영: VM 환경변수에서 주입(파일 저장 지양)
- IV/nonce는 매 암호화마다 랜덤 생성하여 암호문과 함께 저장한다.
- AAD(추가 인증 데이터)는 owner_id, key_version 등을 사용 가능.

### 4.3 키 로테이션(최소 규칙)
- `key_version`을 유지한다.
- 로테이션 시:
  1) 새 마스터키로 재암호화
  2) 이전 마스터키 폐기(운영 환경변수에서 제거)

---

## 5. 푸시(Web Push) 키 관리

- VAPID 키 쌍:
  - `VAPID_PUBLIC_KEY` (클라이언트 노출 가능)
  - `VAPID_PRIVATE_KEY` (절대 외부 노출 금지)
- 클라이언트는 `NEXT_PUBLIC_VAPID_PUBLIC_KEY`로 공개키를 받는다(배포 플랫폼에 설정).

---

## 6. 로그/관측 데이터 취급

- 로그에 키/토큰/쿠키/암호문이 찍히지 않도록 마스킹 처리.
- 에러 로그에 Upbit 요청/응답 raw body 전체를 남기지 않는다.
- Prometheus 메트릭에 식별 가능한 개인정보/시크릿 포함 금지.

---

## 7. 릴리즈 전 체크리스트

- [ ] `.env*`, `application*.yml|yaml` 레포 포함 여부 점검
- [ ] `.cursorignore` 최신화
- [ ] Upbit 키는 DB에 암호문으로만 존재
- [ ] `UPBIT_KEY_MASTER_KEY`, JWT secrets, `VAPID_PRIVATE_KEY`는 운영 환경변수에만 존재
- [ ] Grafana/Jenkins 기본 비밀번호 변경
- [ ] 서버 방화벽: DB/Redis/Admin 포트 외부 차단
