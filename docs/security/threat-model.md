# 위협 모델(최소)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

목표:
- 현실적으로 터질 가능성이 높은 위협을 선별하고,
- 설계/테스트/운영 문서에 연결한다.

---

## 1. 보호 자산(Assets)

P0(유출/변조 시 치명적)
- Upbit Access/Secret(평문) + 암호문
- Upbit 마스터키(`UPBIT_KEY_MASTER_KEY`)
- JWT secrets(Access/Refresh)
- Refresh 세션(Redis `refresh_jti`)
- 주문/체결/포지션/잔고 정합성 데이터
- Kill Switch 및 리스크 설정

P1
- 백테스트 결과/리포트(조작 시 의사결정 왜곡)
- 운영 로그/메트릭(침입 탐지에 영향)
- Push 구독 정보(endpoint/keys) — 직접 금전 피해는 낮지만 프라이버시/스팸 리스크

---

## 2. 공격면(Attack Surface)

- Public: `api.everbit.kr`(Nginx 443)
- OAuth: Kakao redirect/callback
- Outbound: Upbit REST/WS, Kakao API, Web Push endpoints
- Admin Surface: Grafana/Jenkins/Prometheus(기본 비공개)
- VM SSH(22)

---

## 3. 주요 위협 시나리오와 대응

### 3.1 시크릿 커밋/유출
원인:
- `.env`/키 파일을 커밋
- 로그에 토큰/키 출력
- Jenkins credential 노출

대응:
- `docs/security/secrets.md` 규칙 강제
- `.gitignore`/`.cursorignore` 유지
- 운영 시크릿은 `/etc/everbit/everbit.env`(600) 또는 환경변수로만

### 3.2 포트 오픈/관리 UI 노출
원인:
- compose에서 DB/Redis/Admin 포트 publish
- OCI NSG를 0.0.0.0/0으로 열어둠

대응:
- `docs/security/exposure-policy.md` 고정
- Admin Surface는 SSH 터널 기본(ADR-0006)
- 배포 전 포트 스캔(셀프 체크) 수행

### 3.3 인증/인가 우회
원인:
- JWT 검증 누락
- CORS/CSRF 오구성
- OWNER-only 인가 누락

대응:
- OWNER-only 권한 매트릭스(컨트롤러 공통 필터)
- Refresh는 HttpOnly cookie + rotation
- `/actuator/**` 외부 차단

### 3.4 주문 중복/레이스(실거래 사고)
원인:
- 잘못된 멱등키(Upbit identifier 재사용 등)
- timeout 시 자동 재주문

대응:
- DB 유니크 제약 + OrderAttempt 상태머신(SoT)
- UNKNOWN 수렴 + SUSPENDED 전환
- 429/418 정책 강제

### 3.5 레이트리밋 오남용 → 418 차단
원인:
- 그룹별 제한 무시
- 실패 시 과도 재시도

대응:
- UpbitHttpClient 단일 진입점
- 429 즉시 중단 + 백오프
- 418 수신 시 차단 해제까지 호출 금지 + 자동매매 중단

### 3.6 푸시 키/구독 오남용
원인:
- VAPID private key 유출
- 구독 리스트 외부 유출

대응:
- `VAPID_PRIVATE_KEY`는 운영 시크릿(never commit)
- 구독 데이터는 최소 정보만 저장
- 전송 실패(해지)는 자동 정리(스팸/비용 방지)

---

## 4. 탐지/대응(운영)

- 401/403 급증, 429/418 급증은 알림 조건으로 둔다.
- SSH 로그인 로그를 주기 점검한다.
- 주문/킬스위치/키관리 이벤트는 감사 로그로 남긴다(민감값 제외).

---

## 5. 문서 연결

- 시크릿: `docs/security/secrets.md`
- 노출 정책: `docs/security/exposure-policy.md`
- 주문 파이프라인: `docs/architecture/order-pipeline.md`
- 운영 런북: `docs/operations/runbook.md`
