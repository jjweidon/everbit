# ADR-0010: 멀티유저 모델 + Everbit Key 게이팅

Status: **Accepted**  
Date: 2026-03-11  
Supersedes: [ADR-0002](./0002-single-tenant.md)

---

## Context

초기 v2 MVP는 **1인 전용(OWNER 단일) 싱글 테넌트**로 설계되었다(ADR-0002).  
신규 요구사항으로 **복수 사용자 온보딩**이 필요해졌으며, 동시에 **무분별한 서비스 이용을 제한**하기 위한 접근 제어 수단이 필요하다.

구체적 요구사항:
- 카카오 OAuth2를 통해 누구나 회원가입할 수 있어야 한다.
- 다만, 실거래(Upbit 주문/전략 실행) 기능 이용은 두 가지 키를 모두 보유한 사용자로 제한한다:
  1. Upbit API 키 - 사용자가 직접 등록
  2. Everbit Key - 운영자(ADMIN)가 심사 후 발급
- 운영자(ADMIN) 계정은 Everbit Key 심사 대상이 아니며, 별도 Everbit Key 없이 모든 기능을 이용한다.

---

## Decision

### 1. 사용자 모델: 멀티유저(ADMIN / USER)

역할별 설명:

- ADMIN: 운영자. 최초 카카오 로그인 계정이 자동으로 ADMIN이 된다. Everbit Key 불필요, 모든 기능 접근 가능.
- USER: 일반 사용자. 카카오 OAuth2 회원가입 가능. 실거래 이용 전 Everbit Key 필수.

역할은 app_user.role 컬럼(ADMIN / USER)으로 관리한다.  
ADMIN은 1명으로 고정한다(추가 ADMIN 발급은 향후 검토).  
최초 로그인 시 ADMIN으로 생성되며, 이후 로그인은 ADMIN 카카오 계정 또는 신규 USER로 구분된다.

### 2. Everbit Key 발급 흐름

USER 등록(카카오) → 에버비트 키 발급 요청 제출(목적 + 개인 정보) → ADMIN 심사(PENDING → APPROVED/REJECTED) → APPROVED 시 everbit_key 생성 + user.everbit_key_status = ACTIVE → 실거래 기능 이용 가능

- 발급된 Everbit Key 값은 DB에 저장하고, 사용자에게 전달(표시 또는 이메일 등 별도 채널)된다.
- Everbit Key는 운영자가 언제든 REVOKED 처리할 수 있으며, 즉시 실거래 기능이 차단된다.

### 3. 트레이딩 게이트 조건 (ADMIN 제외)

USER가 아래 조건을 모두 충족해야 실거래(OrderAttempt 발행/Upbit 호출)가 허용된다:

1. app_user.everbit_key_status = ACTIVE - 파이프라인 게이트 (Kill Switch 앞)
2. upbit_key 존재 및 유효 - 기존 Upbit Key 게이트 (유지)
3. kill_switch.account_enabled = true - 기존 Kill Switch 게이트 (유지)

ADMIN 역할 보유자는 Everbit Key 조건 검사를 건너뛴다.

### 4. 데이터 모델 추가

- app_user: role, everbit_key_status 컬럼 추가
- 신규 테이블 everbit_key_request: 발급 요청 수명 주기 관리
- 신규 테이블 everbit_key: 발급된 키 저장 (공유 PK, 1:1)

상세: docs/architecture/data-model.md (everbit 관련 테이블)

---

## Consequences

긍정적:
- 운영자가 이용자를 심사한 뒤 서비스를 선택적으로 개방할 수 있다.
- 무단 이용/과도한 API 사용을 사전에 차단할 수 있다.
- ADMIN은 Everbit Key 없이 바로 운영/점검 가능.

부정적/주의:
- 사용자 데이터 격리(multi-tenant isolation)를 철저히 구현해야 한다.
  owner_id FK가 모든 트레이딩 테이블에 존재하므로, API 인가 레이어에서 owner_id = 현재 로그인 사용자를 강제해야 한다.
- ADR-0002 의 "다른 카카오 계정 차단" 정책은 폐기한다.
  대신 ADMIN/USER 역할 분리 + Everbit Key 게이트로 접근을 제어한다.
- 기존 FR-AUTH-002 (OWNER-lock) 요구사항은 아래 FR-AUTH-002 로 대체된다.

---

## Alternatives

- 초대 코드 방식(invite code): 기각 - 코드 유출 시 무단 가입 가능, 발급 이력 추적 어려움
- IP 화이트리스트: 기각 - 사용자 IP가 동적이고 관리 비용 높음
- 심사 없이 Upbit Key만 요구: 기각 - 운영자 개입 없이 누구나 트레이딩 기능 이용 가능
- 계속 싱글 테넌트 유지: 기각 - 복수 사용자 요구사항 충족 불가
