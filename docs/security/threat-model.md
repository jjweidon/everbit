# 위협 모델 (최소)

Status: Draft (운영 스펙 고정)
Last updated: 2026-02-14 (Asia/Seoul)

## 1. 목표

Everbit v2 운영에서 “현실적으로 터질 수 있는” 위협을 정의하고, 문서/설계/테스트에 반영한다.

---

## 2. 핵심 자산(보호 대상)

P0(유출/변조 시 치명적):
- 업비트 API Access/Secret (DB에는 암호문만 저장)
- 업비트 키 암호화 마스터키(`UPBIT_KEY_MASTER_KEY`)
- JWT 시크릿(Access/Refresh)
- 계정 세션/리프레시 토큰(사용 시)
- 주문/체결/포지션 데이터(정합성)
- Kill Switch 상태/리스크 규칙(우회되면 손실 가능)

P1:
- 백테스트 결과/리포트(조작 시 의사결정 왜곡)
- 운영 로그/메트릭(침입 탐지에 영향)

---

## 3. 공격면(Attack Surface)

- `api.everbit.kr`(Nginx 443) 공개 API
- Kakao OAuth2 콜백
- Upbit Open API/WS 연동(Outbound)
- (기본 비공개) Jenkins/Grafana/Prometheus 등 Admin Surface
- CI/CD 파이프라인(Jenkins credential, GHCR token 등)
- VM SSH(22)

---

## 4. 주요 위협 시나리오

### 4.1 시크릿 유출
원인:
- .env 파일/키 파일을 git 커밋
- 로그에 키/토큰 출력
- Jenkins credential 노출

대응:
- `docs/security/secrets.md` 규칙 준수
- `.gitignore`, `.cursorignore` 강제
- 운영 VM 시크릿은 `/etc/everbit/everbit.env`(600)로만 관리

### 4.2 외부 포트 오픈 실수
원인:
- docker compose에서 DB/Redis/Kafka 포트 publish
- OCI NSG/Security List에 광범위 허용

대응:
- `docs/security/exposure-policy.md` 준수(절대 공개 금지 포트 고정)
- 배포 전 “포트 스캔 셀프 체크” 수행

### 4.3 인증/인가 우회
원인:
- JWT 검증 누락, CORS/CSRF 오구성
- Admin endpoint 잘못 노출

대응:
- 엔드포인트별 권한 매트릭스(추가 문서 권장)
- actuator 차단, admin surface 비공개 기본값

### 4.4 주문 중복/레이스
원인:
- 재시도/타임아웃 처리 오류
- 멱등 키 정책 오류

대응:
- `docs/architecture/order-pipeline.md`의 멱등/재시도 정책 준수
- UNKNOWN 수렴 정책으로 안전하게 중단

### 4.5 공급망/의존성 취약점
원인:
- Jenkins 플러그인, Grafana 플러그인, Docker 이미지 취약점

대응:
- 플러그인 최소화
- 정기 업데이트(주간)
- 이미지 태그 고정 + SBOM/스캔(추후)

---

## 5. 운영 탐지/대응

- 로그인/키 등록/주문 생성 이벤트는 감사 로그로 남긴다(민감값 제외)
- 401/403/429/418 급증은 알림 조건으로 둔다
- SSH 접근 로그는 주기 점검(미인가 접근 탐지)

---

## 6. 문서 연결

- 시크릿 규칙: `docs/security/secrets.md`
- 노출 정책: `docs/security/exposure-policy.md`
- Admin 접근: `docs/operations/admin-surface-policy.md`
- 주문 파이프라인: `docs/architecture/order-pipeline.md`
