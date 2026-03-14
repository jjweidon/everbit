# 기능 요구사항 (FRD)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-11 (Asia/Seoul)

우선순위:
- **P0**: MVP 런칭/운영에 필수(미구현 시 거래/운영 불가)
- **P1**: 반드시 구현 예정(단, MVP 이후)
- **P2**: 아이디어/확장(필요 시)

문서 관계:
- 주문/재시도/멱등의 최종 기준: `docs/architecture/order-pipeline.md`
- Event Bus/Queue(Outbox) 기준: `docs/architecture/event-bus.md`
- 전략 상세: `docs/strategies/` (EVERBIT_MASTER_SPEC 및 개별 전략 스펙)

---

## 1. 목표

- 업비트 기반 자동매매 시스템 v2 구축
- 멀티유저(ADMIN/USER) 지원, Everbit Key 심사 기반 접근 제어
- AI 의존 개발 전제: **요구사항/테스트/운영 문서가 코드보다 먼저 고정**되어야 한다.

---

## 2. 사용자/인증/세션

> ADR: [ADR-0010](../adr/0010-multi-user-everbit-key.md)

### FR-AUTH-001 (P0) 카카오 OAuth2 로그인(유일 수단)
**설명**: 로그인/회원가입은 카카오 OAuth2만 제공한다. 누구나 회원가입할 수 있다.

**수용 기준**
- 최초 로그인 계정이 자동으로 `ADMIN` 역할로 등록된다. 이후 로그인하는 계정은 `USER` 역할로 등록된다.
- 로그인 성공 시 서버는 **Access Token + Refresh Token**을 발급한다.
- 토큰 전략(결정 고정):
  - Access Token: `Authorization: Bearer <token>` (클라이언트 저장: 메모리, 필요 시 상태 관리)
  - Refresh Token: **HttpOnly + Secure 쿠키**
  - Refresh rotation: Refresh 사용 시 새 Refresh 발급(재사용 탐지), 서버는 Redis에 `refresh_jti`를 저장한다.
- 로그아웃 시 Refresh 쿠키를 만료시키고 Redis의 `refresh_jti`를 폐기한다.

### FR-AUTH-002 (P0) 역할(ADMIN / USER) 기반 계정 관리
**설명**: 사용자 역할은 `ADMIN`과 `USER` 두 가지이며, 역할별로 접근 범위가 다르다.

**수용 기준**
- `ADMIN`: 최초 가입자. 1명으로 고정. 모든 기능(Everbit Key 심사/발급 포함)에 접근 가능. Everbit Key 불필요.
- `USER`: 일반 사용자. 카카오 OAuth2로 자유롭게 회원가입 가능.
  - 실거래 기능 이용 전 **Everbit Key**를 발급받아야 한다(§3. Everbit Key 관리 참고).
  - Everbit Key가 없거나 `ACTIVE`가 아닌 경우 실거래 관련 엔드포인트는 403으로 차단된다.
- ADMIN이 이미 존재할 때 추가 ADMIN 등록은 허용하지 않는다(향후 검토).

### FR-AUTH-003 (P0) API 인가(역할 기반)
**설명**: 엔드포인트별로 필요한 역할/조건을 강제한다.

**수용 기준**
- 인증 미존재/만료: 401
- 인증은 있으나 권한 부족: 403
- 관리자성 엔드포인트(Everbit Key 심사/발급/폐기, 킬스위치 강제, 전략 설정 등)는 `ADMIN`-only로 고정한다.
- 실거래 엔드포인트(주문 실행/전략 실행 관련)는 `ADMIN` 또는 `everbit_key_status = ACTIVE` USER만 허용한다.
- 자기 데이터 조회/수정은 인증된 사용자 본인(`owner_id = 현재 로그인 사용자`) 범위만 허용한다.

---

## 3. Everbit Key 관리

> ADR: [ADR-0010](../adr/0010-multi-user-everbit-key.md)

### FR-EVERBIT-KEY-001 (P0) Everbit Key 발급 요청
**설명**: USER가 서비스 이용 목적과 개인 정보를 제출하여 ADMIN에게 Everbit Key 발급을 요청한다.

**수용 기준**
- 요청 제출 시 `everbit_key_request` 레코드가 생성된다(status = `PENDING`).
- 동일 사용자가 이미 `PENDING` 또는 `APPROVED` 요청을 보유하면 중복 제출을 허용하지 않는다(409).
- 요청 내용(목적/개인 정보)은 평문으로 DB에 저장한다. 단, 운영 상 민감한 개인 정보 수집은 최소화해야 한다.
- 요청 상태(PENDING/APPROVED/REJECTED)를 사용자가 조회할 수 있어야 한다.

### FR-EVERBIT-KEY-002 (P0) Everbit Key 심사(ADMIN)
**설명**: ADMIN이 발급 요청을 조회하고 승인(APPROVE) 또는 거부(REJECT)한다.

**수용 기준**
- ADMIN 전용 엔드포인트(`ADMIN`-only)로 보호한다.
- 승인(APPROVE) 시:
  - `everbit_key_request.status = APPROVED`로 전환된다.
  - `everbit_key` 레코드가 생성된다(UUID v7 기반 key_value, status = `ACTIVE`).
  - `app_user.everbit_key_status = ACTIVE`로 갱신된다.
  - 감사 로그를 남긴다(reviewed_by, reviewed_at).
- 거부(REJECT) 시:
  - `everbit_key_request.status = REJECTED`로 전환된다.
  - 거부 사유(admin_note)를 선택적으로 입력할 수 있다.
  - `app_user.everbit_key_status`는 `NONE`을 유지한다.
- ADMIN은 대기 중인 요청 목록을 조회할 수 있어야 한다.

### FR-EVERBIT-KEY-003 (P0) Everbit Key 조회(사용자)
**설명**: 사용자가 자신의 Everbit Key 상태와 key_value를 확인한다.

**수용 기준**
- 발급된 key_value는 본인만 조회 가능하다.
- key_value는 최초 발급 후에만 표시되며, 이후 재조회 시에도 표시된다(단, 로그에는 기록하지 않는다).

### FR-EVERBIT-KEY-004 (P0) Everbit Key 폐기(ADMIN)
**설명**: ADMIN이 특정 사용자의 Everbit Key를 폐기한다.

**수용 기준**
- 폐기 즉시 `everbit_key.status = REVOKED`, `app_user.everbit_key_status = REVOKED`로 갱신된다.
- 폐기 즉시 해당 사용자의 실거래 기능이 차단된다.
- 폐기는 감사 로그로 남긴다.
- 폐기된 사용자가 재발급을 원할 경우 다시 FR-EVERBIT-KEY-001 흐름을 거친다.


---

## 4. Upbit API 키 관리

### FR-UPBIT-KEY-001 (P0) API 키 등록
**설명**: Upbit Access/Secret Key를 등록한다.

**수용 기준**
- 등록 즉시 “검증 API 호출(읽기 1회)”을 수행한다.
- 검증 성공 시에만 DB에 저장한다.
- DB에는 **암호문만 저장**한다(평문 저장 금지). 상세는 `docs/security/secrets.md`, ADR-0005.

### FR-UPBIT-KEY-002 (P0) API 키 검증
**설명**: 키 유효성/권한을 검증한다.

**수용 기준**
- 레이트리밋(429) 발생 시 백오프/재시도(단, 과도 호출 금지).
- 검증 결과(성공/실패/시간/사유 코드)를 UI에서 확인할 수 있다.

### FR-UPBIT-KEY-003 (P0) API 키 폐기
**설명**: 저장된 키를 폐기한다.

**수용 기준**
- 키 폐기 즉시 실거래 기능이 비활성화되어야 한다.
- 실행 중인 트레이딩 루프/컨슈머는 “키 없음” 상태를 감지하고 주문을 중단해야 한다.

---

## 5. 전략/설정/실행(전략 포트폴리오 + 여러 마켓)

### FR-TRADE-001 (P0) 전략 실행 단위(전략 포트폴리오 + 여러 마켓)
**설명**: EXTREME_FLIP v1.1, STRUCTURE_LIFT v1.0, PRESSURE_SURGE v1.0 세 전략과 Regime Router·Strategy Arbitration를 조합해 여러 마켓(KRW-*)에 대해 실행한다. 동일 마켓에는 동시에 하나의 owner 전략만 포지션을 가진다.

**수용 기준**
- 활성 마켓 목록을 설정할 수 있다(Enable/Disable).
- 마켓별 상태(포지션/평균단가/보유수량/최근 신호/쿨다운/실행 상태)가 분리 관리된다.
- 포지션 상태(FLAT/OPEN)와 시장 실행 상태(ACTIVE/SUSPENDED)는 별도로 관리된다.
- 동시 포지션은 전략 파라미터(`maxOpenMarkets`)로 제한된다(기본 2).

### FR-TRADE-002 (P0) 전략 설정 CRUD
**설명**: 전략 파라미터를 저장/조회/수정한다.

**수용 기준**
- 전략별 파라미터(EXTREME_FLIP, STRUCTURE_LIFT, PRESSURE_SURGE 등: 타임프레임/임계값/쿨다운/포지션 제한/주문 정책)를 저장/조회할 수 있다.
- 설정 변경은 감사 로그로 남긴다(민감정보 제외).
- 설정 스키마 버전(`config_version`)을 저장하고, 변경 시 이벤트를 발행한다(대시보드 반영).

### FR-TRADE-003 (P0) 주문 금액 최소/최대 설정(KRW 기준)
**설명**: 매수/매도 각각 최소·최대 주문 금액을 설정한다.

**수용 기준**
- “매도=매수 동일 적용” / “매수=매도 동일 적용” UI 버튼 제공.
- 주문 생성 직전 리스크 게이트에서 강제 적용한다.

### FR-TRADE-004 (P0) 리스크 규칙(손절/익절/트레일링)
**설명**: 손절/익절/트레일링을 MVP에 포함한다(세부 파라미터는 전략 설정).

**수용 기준**
- 리스크 규칙은 주문 파이프라인에서 **강제 게이트**로 동작한다.
- 주문이 차단/실행된 이유를 이벤트/로그/대시보드에서 확인할 수 있다(Reason Code).

### FR-TRADE-005 (P0) Kill Switch(계정/전략)
**설명**: 실거래 즉시 중단 장치.

**수용 기준**
- 계정 Kill Switch:
  - UI: 헤더 토글(ON/OFF)
  - OFF 시 신규 주문 생성/전송을 금지한다.
  - OFF 시에도 **Signal/OrderIntent 기록은 허용**한다(분석/감사용).
  - 단, `OrderAttempt` 발행(Upbit 호출) 및 `everbit.trade.command` 스트림(outbox_event) 커맨드 발행은 **금지**한다(상세: `docs/architecture/order-pipeline.md`).
  - 이미 Upbit에 접수된 주문을 자동 취소하지 않는다(P0). 필요 시 수동 정리/향후 확장(P1).
- 전략 Kill Switch:
  - OFF 시 해당 전략 키의 주문 생성/실행을 금지한다.
- Kill Switch는 파이프라인 모든 단계에서 강제되어야 한다(상세: `docs/architecture/order-pipeline.md`).

---

## 6. 주문/멱등/재시도/정합성 (P0)

> 최종 스펙: `docs/architecture/order-pipeline.md`

### FR-ORDER-001 (P0) DB 기반 멱등(SoT) + 상태머신
**설명**: Outbox/Queue(at-least-once) 전제에서도 중복 주문이 발생하지 않도록 **DB가 멱등을 보장**한다.

**수용 기준**
- Signal 유니크: `(owner_id, strategy_key, market, timeframe, candle_close_time, side)`
- OrderIntent 유니크: `(signal_id, intent_type)`
- OrderAttempt는 “Upbit 호출 1회 = Attempt 1개”로 모델링한다.
- 재기동/재처리 시 동일 Signal/Intent에 대해 중복 주문이 생성되지 않는다.

### FR-ORDER-002 (P0) Upbit identifier 정책(Attempt 단위, 재사용 금지)
**설명**: Upbit `identifier`는 조회/상관관계 용도이며, 멱등키가 아니다.

**수용 기준**
- identifier는 Attempt 생성 시 신규 생성(예: UUID v7).
- 동일 identifier 재사용 금지.
- 주문 조회는 uuid 또는 identifier로 수행 가능하다(필요 시).

### FR-ORDER-003 (P0) UNKNOWN 수렴 및 시장 단위 SUSPENDED
**설명**: timeout/네트워크/5xx 등 “주문 생성 여부 불확실” 상황에서 중복 주문 방지를 최우선한다.

**수용 기준**
- CreateOrder timeout/5xx 발생 시 Attempt는 `UNKNOWN`으로 기록한다.
- reconcile(조회) 후에도 확정 실패 시 해당 마켓은 `market_state.trade_status = SUSPENDED`로 전환한다.
- `SUSPENDED` 마켓은 수동 해제 전까지 신규 주문 생성/Attempt 발행이 금지된다.

### FR-ORDER-004 (P0) 레이트리밋 준수(429/418)
**설명**: 레이트리밋 위반이 연쇄 장애/차단으로 이어지지 않도록 강제한다.

**수용 기준**
- 429: 해당 그룹 호출 즉시 중단 + 백오프 + 이후 새 Attempt로 재시도(THROTTLED).
- 418(차단): 차단 해제 시각까지 **재시도 금지**, 자동매매 중단(운영 경고/알림).

---

## 7. 푸시 알림(필수)

> “시그널 발생으로 매수·매도 주문 접수 시” 클라이언트 푸시 알림 수신을 P0로 고정한다.

### FR-NOTI-001 (P0) 주문 접수(Upbit ACK) 푸시 알림
**설명**: 매수/매도 주문이 Upbit에 접수(ACK)되면 클라이언트에 푸시 알림을 전송한다.

**수용 기준**
- 트리거: `OrderAccepted`(Upbit UUID 확보) 이벤트 발생 시
- 알림 내용(최소):
  - side(BUY/SELL), market, intentType, 주문금액/수량, 발생 시각, 전략Key
- 알림은 **중복 전송이 가능**하므로(푸시 채널 특성), 클라이언트는 `eventId` 기반으로 중복 표시를 방지할 수 있어야 한다.

### FR-NOTI-002 (P0) 푸시 구독(등록/해지) 및 권한 UX
**설명**: 클라이언트는 푸시 권한 요청 및 구독 등록/해지가 가능해야 한다.

**수용 기준**
- 사용자가 명시적으로 “푸시 알림 사용”을 켜면:
  - Service Worker 등록
  - Push subscription 생성
  - 서버에 구독 등록 API 호출
- 사용자가 끄면:
  - 서버 구독 해지 API 호출
  - (가능하면) 브라우저 subscription 해지
- UX 원칙:
  - 권한 요청은 첫 진입에서 강제하지 않는다(명시적 동작에서 요청).
  - 권한 거부 상태를 UI에서 명확히 안내한다.

### FR-NOTI-003 (P0) 푸시 실패 정리(구독 청소)
**설명**: 푸시 전송 실패(만료/해지) 구독을 자동 정리한다.

**수용 기준**
- 전송 실패가 “구독 만료/해지(예: 410 Gone)”인 경우 해당 구독을 DB에서 비활성화/삭제한다.
- 단일 계정이지만, 여러 디바이스/브라우저 구독을 허용한다.

---

## 8. 백테스트

### FR-BT-001 (P0) 멀티 마켓/멀티 타임프레임 백테스트
**설명**: 멀티 마켓/멀티 타임프레임 백테스트를 실행한다.

**수용 기준**
- 입력: 마켓 리스트, 타임프레임 리스트, 기간, 초기자본, 수수료/슬리피지 옵션
- 출력(최소): CAGR, MDD, 승률, 수익 팩터
- 결과는 DB에 저장되고 조회 가능해야 한다.
- 동일 설정 재실행 시 실행 이력을 남긴다(전략 버전 포함).

### FR-BT-002 (P0) 지표 산식 고정
**설명**: 지표 산식은 문서로 고정한다.

**수용 기준**
- 산식/가정은 `docs/testing/performance-plan.md`에 고정하며, 변경 시 ADR이 필요하다.

### FR-BT-003 (P1) 페이퍼 트레이딩/시뮬레이션
**설명**: 실거래 전 동일 파이프라인으로 시뮬레이션을 제공한다.

**수용 기준**
- REAL/PAPER 모드가 명확히 분리된다.
- PAPER에서는 Upbit 주문 호출을 하지 않고 시뮬레이션 체결 모델로 대체한다.
- 대시보드도 REAL/PAPER를 분리 표시한다.

---

## 9. 대시보드/조회

### FR-UI-001 (P0) 실행 상태
- 전략 실행 ON/OFF
- 마켓별 상태(포지션/쿨다운/SUSPENDED)
- 마지막 실행/에러 시각
- 429/418/UNKNOWN 상태 요약

### FR-UI-002 (P0) 최근 주문/잔고/손익
- 최근 주문 목록(상태/체결/시간/사유)
- 현재 잔고(현금 + 보유자산)
- 손익(실현/미실현 최소 구분)

### FR-UI-003 (P1) 확장 대시보드
- 슬리피지/체결 분석
- 전략별 성과 비교
- 리스크 지표 상세(노출/손실 제한)

---

## 10. 설정 변경/감사 로그

### FR-AUDIT-001 (P0) 감사 로그(민감값 제외)
**설명**: 거래/보안과 관련된 핵심 이벤트는 감사 로그를 남긴다.

**수용 기준**
- 예: 로그인, 키 등록/폐기, Kill Switch 변경, 전략 설정 변경, 주문 상태 변화, SUSPENDED 전환
- 민감값(키/토큰/쿠키/암호문)은 로그에 포함하지 않는다.
