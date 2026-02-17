# AGENT.md — Everbit v2 Cursor Agent Rules

Status: **Mandatory**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)  
Scope: **개발/기획/문서/테스트/운영 전 과정**

이 문서는 Cursor(Composer/Auto 포함)가 everbit v2 작업을 수행할 때 **반드시 준수해야 하는 규칙**을 고정한다.  
규칙 위반은 “작동은 해도 운영/보안/정합성에서 망가지는 코드”를 양산하므로, 기능 구현보다 우선한다.

---

## 0. 프로젝트 운영 모델(고정)

- Branch SoT: **`v2` 브랜치**
- 문서 SoT: **`docs/**`**
- 코드 SoT: 문서 결정에 종속
- 사용자 모델: **1인 전용(OWNER 단일)**, 로그인은 **카카오 OAuth2 단일 수단**
- 거래소: **Upbit** 단일
- DB: **PostgreSQL**
- 실행: **단일 VM 올인원(OCI 우선)**

---

## 1. 문서 우선(Documentation-first) 강제 규칙

### 1.1 결정 계층(상위가 항상 우선)
1) `docs/requirements/*` (FRD/NFR)
2) `docs/architecture/*` (SoT)
3) `docs/integrations/*`
4) `docs/security/*`
5) `docs/testing/*`
6) `docs/operations/*`
7) `docs/strategies/*`
8) `docs/adr/*`
9) 코드

### 1.2 구현 착수 전 “문서 고정” 조건
아래 항목이 문서로 고정되지 않으면 구현을 시작하지 않는다.
- API DTO(요청/응답) + 에러코드/Reason Code
- 상태 머신/멱등키/재시도 정책(특히 주문)
- 외부 연동 규칙(Upbit 429/418/WS, Kakao OAuth)
- 보안 규칙(시크릿/키 저장/노출 포트)

### 1.3 문서 변경 의무
- 요구/정책/흐름이 바뀌면 **코드보다 문서를 먼저 수정**한다.
- “문서와 코드가 불일치”하면 문서를 기준으로 코드가 수정되어야 한다.
- 큰 결정 변경은 ADR이 필요하다(2장 참고).

---

## 2. ADR(Architecture Decision Record) 규칙

### 2.1 ADR이 필요한 변경(필수)
- 인증/세션 전략 변경(JWT/쿠키/CSRF)
- 주문 파이프라인 정책 변경(UNKNOWN/멱등/재시도/레이트리밋)
- DB 변경(PostgreSQL → 다른 DB, 핵심 테이블/유니크/인덱스 수정)
- 인프라/노출 정책 변경(포트 공개, Admin Surface 공개)
- Kafka 제거/대체/토픽 전략 변경
- 전략(EXTREME_FLIP) 의사결정 규칙 변경

### 2.2 ADR 작성 규칙
- 파일명: `docs/adr/NNNN-title.md`
- 포함 필수: Context / Decision / Consequences / Alternatives
- 결정은 “모호한 가능성”이 아니라 “고정 규칙” 형태로 작성한다.

---

## 3. Git/동기화 운영 규칙(필수)

### 3.1 SoT 동기화 방식(강제)
- GitHub `v2` 브랜치가 진짜 기준이다.
- ChatGPT/외부 도구로부터는 **전체 파일이 아니라 unified diff(패치)만** 받는다.
- 패치 적용은 아래 흐름을 따른다.

권장 적용:
- `pbpaste | git apply -p1 --check`
- `pbpaste | git apply -p1`
- `git status && git diff`

### 3.2 커밋 규칙
- 커밋 prefix: `docs:`, `feat:`, `fix:`, `test:`, `chore:`, `refactor:`
- 문서 변경은 단독 커밋으로 분리한다(리뷰/롤백 용이).
- “대량 자동 포맷/전체 교체” 커밋 금지. 변경 범위는 최소화한다.

### 3.3 PR 규칙(혼자여도 권장)
- 문서/정책 변경은 PR 단위로 남겨서 이력을 명확히 한다.
- PR에는 반드시 “왜(결정)”와 “무엇(영향)”를 남긴다.

---

## 4. 보안/시크릿 규칙(절대 위반 금지)

### 4.1 절대 커밋 금지
- `.env*`, `application*.yml|yaml`(시크릿 포함 가능)
- Upbit Access/Secret(평문), JWT secret, Kakao client secret
- `UPBIT_KEY_MASTER_KEY`, `VAPID_PRIVATE_KEY`
- TLS private key, Jenkins/Grafana 관리자 비밀번호/토큰
- 실거래 로그 원문(자산/주문 상세 포함)

### 4.2 저장 정책(고정)
- 로컬: `.env.local`
- 운영: VM 환경변수 또는 `/etc/everbit/everbit.env`(권한 600)
- Upbit API 키는 DB에 저장하되 **암호문만 저장**(AES-GCM). 마스터키는 운영 환경변수로만 주입.

### 4.3 노출 정책(고정)
- 외부 공개 포트: **80/443/22(내 IP /32 제한)** 외 금지
- Grafana/Jenkins/Prometheus/DB/Redis/Kafka 등 Admin Surface는 외부 노출 금지
- 접근 표준: SSH 터널

### 4.4 로깅 금지 항목
- Access/Secret Key, JWT, Refresh cookie 값, VAPID private key, 암호문 원문, 쿼리 해시 원문
- endpoint 같은 민감값은 마스킹/해시로만 남긴다.

---

## 5. 트레이딩/주문 파이프라인 불변 조건(최상위 SoT)

아래 규칙은 단순 권고가 아니라 “운영 안정성”을 위한 강제 스펙이다. 변경하려면 문서+ADR이 필요하다.

### 5.1 DB가 SoT, Kafka는 at-least-once
- 중복 소비는 정상이다.
- 멱등은 DB 유니크 제약 + 상태 머신으로 보장한다.

### 5.2 Upbit identifier는 멱등키가 아니다
- identifier 재사용 금지
- Attempt(Upbit 호출 1회) 단위로 새 identifier 생성
- 멱등은 Signal/Intent/Attempt 테이블 제약으로 보장한다.

### 5.3 UNKNOWN 수렴 정책(중복 주문 방지)
- timeout/네트워크/5xx는 재주문으로 해결하지 않는다.
- Attempt를 UNKNOWN으로 기록 후 reconcile로 확정
- 확정 불가 또는 위험 시 시장 단위 SUSPENDED로 중단

### 5.4 429/418 정책
- 429: 즉시 감속 + 백오프, 새 Attempt로 재시도(폭주 금지)
- 418: 즉시 호출 중단 + 자동매매 중단(Kill Switch OFF 유도/자동), 수동 해제 전 재개 금지

### 5.5 Kill Switch 의미(고정)
- OFF일 때:
  - Signal/OrderIntent 기록은 허용(분석/감사)
  - **OrderAttempt 발행 + Upbit 호출은 금지**
- 이미 접수된 주문 자동 취소는 P0에서 하지 않는다(향후 P1).

---

## 6. Upbit 연동 규칙(필수)

- 브라우저에서 Upbit 직접 호출 금지(서버에서만 호출)
- Remaining-Req 기반 rate state를 파싱하고, “429를 받으며 학습”하는 방식 금지
- Private WS(myOrder/myAsset)는 Authorization 헤더로 연결
- myAsset 초기 지연 가능성을 전제로, 연결 직후 `/v1/accounts` 스냅샷 동기화 권장
- WS 재연결은 지수 백오프, keepalive(ping) 필수

---

## 7. 푸시 알림(Web Push) 규칙(필수)

- 트리거: **OrderAccepted(Upbit UUID 확보)**
- 전달 보장: best effort, 중복 가능 → payload에 `eventId` 포함
- deepLink:
  - 상대 경로만 허용(`/`로 시작)
  - 외부 URL/스킴 금지
- 실패 코드 정리:
  - 404/410: 구독 즉시 비활성화
  - 429/5xx/timeout: 비활성화 금지(자연 재시도)
  - 400/401/403: 구성 오류 가능성 → 로그/분석 우선

---

## 8. Frontend(Next.js) 규칙

### 8.1 구조/품질
- 도메인 단위로 폴더를 분리한다(거대 공통 폴더 남발 금지).
- Zustand store는 “작고 명확한 스토어 여러 개”로 분해한다.
- 네트워크/상태/컴포넌트 로직이 한 파일에 과밀하면 분리한다.
- 매직 넘버/문자열 상수는 명명 상수로 추출한다.

### 8.2 UX 규칙(운영 안정성)
- 실거래는 “오동작/오해”가 손실로 직결된다.
- 위험 작업(킬 스위치, 키 폐기)은 확인 UX를 둔다.
- 상태는 명시적으로 보여준다:
  - RUNNING / STOPPED / SUSPENDED / THROTTLED / UNKNOWN
- 알림 권한 요청은 사용자 명시적 액션에서만.

### 8.3 디자인 시스템
- `docs/design/ui-ux-concept.md`를 기준으로 다크 테마와 포인트 컬러를 적용한다.
- 컴포넌트 상태(hover/active/disabled/error/success) 표현을 일관되게 유지한다.

---

## 9. Backend(Spring Boot) 규칙

### 9.1 레이어링(강제)
- Controller: DTO/검증/인가만
- Service/Domain: 비즈니스 규칙
- Adapter: Upbit/Kakao/WebPush 외부 연동 캡슐화
- Repository: persistence
- 외부 호출(Upbit 등)을 Controller/Domain에 직접 박지 않는다.

### 9.2 트랜잭션 규칙
- DB 트랜잭션 안에서 외부 API 호출 금지(데드락/지연/중복 위험)
- Outbox 패턴으로 “DB 변경 + 이벤트 발행”을 일관성 있게 처리한다.

### 9.3 에러/Reason Code
- 사용자가/운영자가 원인을 바로 파악할 수 있도록 Reason Code를 표준화한다.
- 전략/리스크/실행 단계에서 어떤 게이트에 의해 차단됐는지 기록한다.

---

## 10. 테스트/검증 규칙(필수)

### 10.1 P0 변경 시 반드시 추가되는 테스트
- 주문 멱등(중복 소비)
- 429 처리(THROTTLED → 새 Attempt 재시도)
- 418 처리(중단/재개 금지)
- timeout/5xx → UNKNOWN 수렴 + SUSPENDED 전환
- Kill Switch 강제(Attempt 발행 금지)
- 푸시: 410/404 구독 정리

### 10.2 도구 기준(권장)
- Backend: JUnit5 + Testcontainers(Postgres/Redis/Kafka) + WireMock(Upbit)
- Frontend: Vitest + RTL + Playwright
- 성능/회귀: 작은 스모크는 CI, 큰 테스트는 로컬/야간 분리

---

## 11. 운영/관측(Observability) 규칙

- traceId(상관관계 ID)를 API ↔ Kafka ↔ DB 이벤트에 일관 적용
- 메트릭은 “실거래 안전 신호” 중심으로 노출한다:
  - UNKNOWN/THROTTLED/418, Kill Switch, SUSPENDED, Kafka lag, outbox pending
- 알람은 소음이 아니라 “즉시 행동 가능한 트리거”여야 한다(418/UNKNOWN은 필수).

---

## 12. 문서 작성 스타일 규칙(필수)

- 파일명은 원칙적으로 kebab-case
- 문서 헤더에 Status/Owner/Last updated 포함
- SoT 문서는 중복 설명을 최소화하고, 다른 문서에서는 링크로 참조한다.
- “규칙”은 반드시 수용 기준/조건/예외 케이스로 내려쓴다.
- 변경 시:
  - 링크 깨짐 확인
  - README 네비게이션 갱신
  - 충돌 문구 제거

---

## 13. 금지 목록(즉시 중단 사유)

- 시크릿/키/토큰을 문서나 코드에 하드코딩/커밋
- Admin 포트(DB/Redis/Kafka/Grafana/Jenkins/Prometheus)를 인터넷에 오픈
- Upbit 주문 생성에서 timeout/5xx를 자동 재주문으로 처리
- Upbit identifier를 멱등키로 사용
- 브라우저에서 Upbit 직접 호출
- 대량 변경(전체 리포맷/파일 대체) 커밋

---

## 14. PR/작업 완료 체크리스트(커밋 전 반드시)

- 문서:
  - 변경된 정책이 docs에 반영되어 있는가?
  - SoT 문서와 내용 충돌이 없는가?
- 보안:
  - `.gitignore`/`.cursorignore` 최신인가?
  - 시크릿이 diff에 포함되지 않았는가?
- 테스트:
  - P0 경로(주문/레이트리밋/UNKNOWN/Kill Switch/푸시 정리) 테스트가 포함되는가?
- 운영:
  - 포트 노출/접근 정책을 위반하지 않는가?
  - 알람/메트릭 계약이 깨지지 않는가?

---

## 15. Cursor 작업 지침(요청-응답 형태)

Cursor는 작업 수행 시 다음 형식을 따른다.
- “무엇을 바꿀지”를 먼저 요약(파일/모듈/영향 범위)
- 변경 후:
  - 관련 문서 링크를 주석/PR 설명에 포함
  - 테스트 추가/수정 여부 명시
  - 보안 영향 여부 명시
- 애매하면 구현을 진행하지 말고, 문서(SoT)부터 고정한다.
