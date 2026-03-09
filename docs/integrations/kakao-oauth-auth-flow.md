# 카카오 OAuth 기반 인증/인가 설계 문서

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

목표:
- 카카오 OAuth를 소셜 로그인 인증 수단으로 사용하는 everbit v2의 인증/인가 구조를 구현 가능한 수준으로 고정한다.
- Next.js + React 클라이언트, Spring Boot 서버 구조를 기준으로 구체화한다.
- 인증/인가의 책임을 백엔드가 지도록 설계한다.

관련 문서(SoT):
- `docs/requirements/functional.md` (FR-AUTH-001~003)
- `docs/requirements/non-functional.md` §1.2
- `docs/adr/0007-auth-session.md`
- `docs/api/contracts.md`
- `docs/security/threat-model.md`
- `docs/architecture/data-model.md`

---

## 부록 A. 참고 자료 분석

### A.1 참고 자료 핵심 주장 요약

> **참고**: [개발자 유미 카페 296](https://cafe.naver.com/xxxjjhhh/296), [349](https://cafe.naver.com/xxxjjhhh/349)는 로그인 제한으로 본문을 직접 확인하지 못했다. 아래는 사용자 지정 주제 및 OAuth 2.0 BCP/실무 패턴을 반영한 요약이다.

| 주제 | 핵심 주장 |
|------|-----------|
| **인증/인가 책임** | 백엔드가 인증·인가의 주체가 되어야 한다. 클라이언트는 토큰 보관·전달만 담당한다. |
| **토큰 사용 추적** | Refresh 토큰 사용 이력을 서버에서 추적하여 재사용 탐지(탈취 대응)를 수행한다. |
| **다중 토큰 생명 주기** | Access Token은 짧게(15분), Refresh Token은 길게(14일). Refresh 사용 시 Rotation 적용. |
| **다중 토큰 구현 포인트** | Access는 Bearer header, Refresh는 HttpOnly cookie. 클라이언트는 카카오 토큰을 직접 보관하지 않는다. |
| **Refresh 토큰 탈취** | Rotation + 재사용 탐지. 재사용 감지 시 해당 grant 전체 무효화, 사용자 재로그인 유도. |
| **토큰 저장 위치** | Access: 메모리(또는 상태 관리). Refresh: HttpOnly + Secure + SameSite=Lax cookie. |
| **Refresh 토큰 Rotate** | 사용 시마다 새 Refresh 발급, 기존 무효화. jti 기반 재사용 탐지. |
| **로그아웃·Refresh 주도권** | 로그아웃은 서버가 주도한다. Refresh 쿠키 폐기 + 서버 저장소(jti) 무효화를 동시 수행. |

### A.2 참고 자료의 문제점·누락점·모호점

| 구분 | 내용 |
|------|------|
| **누락** | 카카오 식별자(id)와 서비스 userId 매핑 전략이 명시되지 않은 경우가 많다. |
| **모호** | "카카오 로그인 성공"과 "서비스 로그인 완료"의 경계가 혼재된다. |
| **누락** | 탈퇴 후 재가입, 동일 이메일·상이 provider 계정 충돌 처리 정책. |
| **모호** | 소셜 계정만으로 가입 완료로 볼지, 추가 약관/프로필 입력 필요 여부. |
| **누락** | 운영 로그/감사 추적 포인트(로그인/로그아웃/재발급/403) 상세. |
| **모호** | BFF 사용 여부, JWT vs 세션 선택 근거. |
| **추정 보완** | Reuse Detection Window(동시 요청 허용 구간)는 OAuth 2.0 BCP 권장을 따르되, everbit는 1인 전용이라 단순화 가능. |

---

## 부록 B. 최종 보완 문서

---

## 1. 문서 목적

본 문서는 everbit v2의 카카오 OAuth 기반 인증/인가를 **구현 가능한 수준**으로 설계·고정한다.

- 개발자, 리뷰어, 아키텍트가 설계 의사결정과 책임 경계를 이해할 수 있도록 한다.
- 모호한 표현 대신 정책형 문장으로 작성한다.
- "가능하다"가 아닌 "본 시스템은 ~로 한다" 형식으로 명확히 한다.

---

## 2. 범위

| 포함 | 제외 |
|------|------|
| 카카오 OAuth2 Authorization Code Flow | 카카오 토큰 직접 클라이언트 보관 |
| 서비스 자체 Access/Refresh Token 발급 | BFF 레이어(v2 MVP) |
| OWNER 1인 전용 계정 락 | 다중 사용자/다중 소셜 연동 |
| 로그인/로그아웃/토큰 재발급 | 카카오 채널/메시지 API |
| Next.js + Spring Boot 구조 | 네이티브 앱/다른 클라이언트 |

---

## 3. 용어 정의

| 용어 | 정의 |
|------|------|
| **OAuth 2.0** | 리소스 소유자 대신 제한된 권한을 위임받기 위한 인가 프레임워크. everbit는 "소셜 로그인" 목적으로 사용한다. |
| **Authorization Code** | 인가 서버(카카오)가 사용자 인증 후 발급하는 일회용 코드. 클라이언트가 직접 교환하지 않고, **백엔드가** 토큰으로 교환한다. |
| **Access Token** | 리소스 접근 권한을 나타내는 토큰. everbit 맥락에서는 **서비스 자체 Access Token(JWT)**을 의미한다. 카카오 Access Token은 서버 내부에서만 사용 후 폐기한다. |
| **Refresh Token** | Access Token 만료 시 재발급에 사용하는 토큰. everbit는 **서비스 자체 Refresh Token**을 발급하며, HttpOnly cookie로 전달한다. |
| **Resource Server** | 보호된 리소스를 제공하는 서버. everbit API 서버가 해당 역할을 한다. |
| **Client** | everbit Next.js 프론트엔드. 사용자 대리로 인증 요청을 시작하지만, **토큰 교환·보관은 백엔드가 수행**한다. |
| **Redirect URI** | OAuth 인증 완료 후 카카오가 리다이렉트하는 URI. everbit 백엔드 콜백 엔드포인트(`/api/v2/auth/callback`)로 고정한다. |
| **인증(Authentication)** | "누구인가"를 확인하는 행위. 카카오 OAuth로 사용자 신원을 검증한다. |
| **인가(Authorization)** | "무엇을 할 수 있는가"를 결정하는 행위. everbit는 OWNER-only로, 인증된 사용자가 OWNER인지 검사한다. |
| **소셜 로그인** | 외부 IdP(카카오)를 통해 사용자 신원을 확인하고, 그 결과를 서비스 계정과 연결하는 방식. |

---

## 4. 요구사항

### 4.1 사용자 관점

| ID | 요구사항 | 수용 기준 |
|----|----------|-----------|
| U-1 | 카카오 계정으로 로그인 | "카카오로 로그인" 버튼 클릭 시 카카오 인증 페이지로 이동 후, 성공 시 everbit 대시보드로 진입한다. |
| U-2 | 로그인 상태 유지 | Access Token 만료 시 자동으로 Refresh로 갱신되며, 사용자는 재로그인 없이 서비스를 이용한다. |
| U-3 | 명시적 로그아웃 | 로그아웃 시 모든 세션이 폐기되고, 재로그인 전까지 보호된 페이지에 접근할 수 없다. |
| U-4 | 1인 전용 제한 인지 | OWNER가 이미 존재할 때 다른 카카오 계정으로 로그인 시도 시, "이 서비스는 등록된 계정만 사용할 수 있습니다" 등 명확한 안내를 받는다. |

### 4.2 프론트엔드 관점

| ID | 요구사항 | 수용 기준 |
|----|----------|-----------|
| FE-1 | 카카오 토큰 미보관 | 카카오 Access/Refresh 토큰을 localStorage/sessionStorage/메모리에 저장하지 않는다. |
| FE-2 | 서비스 Access Token만 메모리 보관 | 서비스 Access Token은 React state/context 등 메모리에만 보관한다. |
| FE-3 | 401 시 Refresh 1회 시도 | API 401 수신 시 `POST /api/v2/auth/refresh` 1회 호출 후, 성공 시 원 요청 1회 재시도. 실패 시 `/login` 리다이렉트. |
| FE-4 | 403 시 로그인 유도 | OWNER 아님(403) 시 에러 카드 + 로그인 페이지 유도. |
| FE-5 | 보호 라우트 | 인증되지 않은 사용자가 보호 라우트 접근 시 `/login`으로 리다이렉트한다. |

### 4.3 백엔드 관점

| ID | 요구사항 | 수용 기준 |
|----|----------|-----------|
| BE-1 | 카카오와 직접 통신 | 인가 코드 → 토큰 교환, 사용자 정보 조회는 **백엔드만** 수행한다. |
| BE-2 | 서비스 토큰 발급 | 카카오 인증 성공 후 서비스 자체 Access(JWT) + Refresh(서버 저장)를 발급한다. |
| BE-3 | Refresh Rotation | Refresh 사용 시 새 Refresh 발급, 기존 jti 무효화. |
| BE-4 | 재사용 탐지 | 동일 Refresh jti 재사용 감지 시 해당 grant 전체 무효화, 401 반환. |
| BE-5 | OWNER 락 | 최초 로그인 계정을 OWNER로 고정. 이후 다른 카카오 계정 로그인 시 403. |
| BE-6 | 토큰 사용 추적 | Refresh 사용/폐기 이벤트를 감사 로그로 남긴다(민감값 제외). |

### 4.4 보안 관점

| ID | 요구사항 | 수용 기준 |
|----|----------|-----------|
| S-1 | state 파라미터 | OAuth 요청 시 state를 생성·검증하여 CSRF를 방지한다. |
| S-2 | Redirect URI 검증 | 카카오 앱 설정의 Redirect URI와 요청 redirect_uri가 정확히 일치해야 한다. |
| S-3 | HttpOnly Cookie | Refresh Token은 HttpOnly + Secure + SameSite=Lax cookie로 전달한다. |
| S-4 | Origin/Referer 검증 | refresh/logout 등 cookie 기반 엔드포인트는 Origin/Referer allowlist(`https://everbit.kr`) 검증을 적용한다. |
| S-5 | 로그 마스킹 | 토큰/키/쿠키 값은 로그에 기록하지 않는다. |

---

## 5. 전체 아키텍처

### 5.1 구성 요소

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Next.js Client │     │ Spring Boot API │     │  Kakao OAuth    │
│  (everbit.kr)   │     │ (api.everbit.kr)│     │  (kauth.kakao.) │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │ 1. GET /auth/start    │                       │
         │──────────────────────>│                       │
         │ 302 → Kakao            │                       │
         │<──────────────────────│                       │
         │                       │                       │
         │ 2. Redirect to Kakao  │                       │
         │──────────────────────────────────────────────>│
         │                       │                       │
         │ 3. User auth at Kakao │                       │
         │<──────────────────────────────────────────────│
         │                       │                       │
         │ 4. Redirect + code    │                       │
         │──────────────────────>│                       │
         │  GET /auth/callback    │ 5. Token exchange     │
         │  ?code=...&state=...   │──────────────────────>│
         │                       │ 6. User info          │
         │                       │──────────────────────>│
         │                       │<──────────────────────│
         │                       │                       │
         │ 7. Set-Cookie + 302   │                       │
         │  (Refresh) + Body     │                       │
         │  (Access)              │                       │
         │<──────────────────────│                       │
```

### 5.2 시스템 역할

| 구성 요소 | 역할 |
|-----------|------|
| **Next.js** | 로그인 버튼 렌더링, 카카오 redirect 시작, 콜백 수신 후 Access Token 저장, 보호 라우트 처리, 401/403 UX |
| **Spring Boot** | OAuth 시작 URL 생성, 콜백 처리, 카카오 토큰 교환·사용자 정보 조회, 회원 매핑, 서비스 토큰 발급, Refresh 저장·검증·Rotation, 로그아웃 |
| **Kakao** | 사용자 인증, Authorization Code 발급, 토큰 교환, 사용자 정보 제공 |
| **Redis** | Refresh jti 저장(로컬). 운영은 환경에 따라 DB 또는 Redis. |

### 5.3 데이터 흐름 개요

1. **로그인 시작**: 클라이언트 → `GET /api/v2/auth/start` → 서버가 state 생성·저장 후 302 Redirect to Kakao
2. **카카오 인증**: 사용자가 카카오에서 로그인/동의
3. **콜백**: 카카오 → `GET /api/v2/auth/callback?code=...&state=...` (백엔드 URL)
4. **토큰 교환**: 백엔드가 code로 카카오에 토큰 요청 → 카카오 Access Token 수신
5. **사용자 정보**: 백엔드가 카카오 /v2/user/me 호출 → kakao_id, email 등 수신
6. **회원 매핑**: kakao_id로 app_user 조회. 없으면 신규 생성(OWNER 락 검사)
7. **서비스 토큰 발급**: Access JWT + Refresh(저장 후 jti) 생성
8. **응답**: Set-Cookie(Refresh) + 302 to 프론트 + Body 또는 Fragment에 Access(선택적)

---

## 6. 로그인/회원가입 시나리오

### 6.1 최초 로그인

- kakao_id에 해당하는 app_user가 없음.
- 신규 app_user 생성, role=OWNER로 고정.
- 서비스 Access + Refresh 발급, 로그인 완료.

### 6.2 기존 회원 로그인

- kakao_id로 app_user 존재.
- 서비스 Access + Refresh 발급, 로그인 완료.

### 6.3 추가 정보 입력이 필요한 경우

- **v2 MVP**: 카카오 프로필(닉네임, 프로필 이미지)만으로 가입 완료. 추가 약관/프로필 입력은 P1.
- **추정 보완**: 향후 이메일 필수화 시, 카카오 이메일 미제공 시 "이메일 입력" 단계 추가 가능.

### 6.4 카카오 연동 해제 또는 미가입 사용자 처리

| 상황 | 처리 |
|------|------|
| OWNER 존재 + 다른 kakao_id 로그인 | 403, "이 서비스는 등록된 계정만 사용할 수 있습니다." |
| 카카오에서 연동 해제 후 재로그인 | 카카오는 동일 kakao_id로 재인증. everbit app_user는 유지. (카카오 연동 해제 ≠ everbit 탈퇴) |
| everbit 탈퇴 후 동일 카카오 재가입 | 정책에 따라: (A) 동일 app_user 복구 또는 (B) 신규 생성. v2 MVP는 (B)로, 기존 데이터는 soft-delete 또는 별도 보존. |
| 동일 이메일·상이 provider | v2는 카카오 단일이므로 해당 없음. |

---

## 7. OAuth 상세 플로우

### 7.1 인가 코드 요청

- **엔드포인트**: `GET /api/v2/auth/start`
- **동작**: state(UUID) 생성, 세션 또는 Redis에 `state → (created_at)` 저장(만료 10분), Kakao 인증 URL 생성 후 302 Redirect.
- **Kakao URL 파라미터**: `client_id`, `redirect_uri`, `response_type=code`, `state`, `scope`(프로필, 이메일 등)

### 7.2 Redirect 처리

- **엔드포인트**: `GET /api/v2/auth/callback?code=...&state=...&error=...`
- **검증**: state 존재·일치·미만료. error 파라미터 있으면 에러 처리.

### 7.3 서버의 토큰 교환

- **카카오 API**: `POST https://kauth.kakao.com/oauth/token`
- **Body**: `grant_type=authorization_code`, `client_id`, `redirect_uri`, `code`, `client_secret`
- **응답**: `access_token`, `refresh_token`, `expires_in` 등. **카카오 토큰은 서버 메모리에서만 사용 후 폐기.**

### 7.4 사용자 정보 조회

- **카카오 API**: `GET https://kapi.kakao.com/v2/user/me`
- **Header**: `Authorization: Bearer {카카오 access_token}`
- **응답**: `id`(kakao_id), `kakao_account.email` 등.

### 7.5 서비스 회원 식별/매핑

- `kakao_id`를 UNIQUE 키로 `app_user` 조회.
- 없으면: OWNER 존재 여부 확인. 없으면 신규 생성. 있으면 403.
- 있으면: 해당 사용자로 로그인 진행.

### 7.6 자체 토큰 발급

- **Access Token**: JWT, `sub=owner_id(또는 public_id)`, `exp=15분`, 서명에 HS256 등.
- **Refresh Token**: 랜덤 문자열(UUID 등), jti로 Redis/DB에 저장. `owner_id`, `expires_at`(14일) 등.

---

## 8. 토큰 전략

### 8.1 카카오 Access Token 사용 범위

- **사용**: 콜백 시 토큰 교환 후 `/v2/user/me` 1회 호출에만 사용.
- **보관**: 사용 후 즉시 폐기. 클라이언트에 전달하지 않는다.

### 8.2 서비스 자체 Access/Refresh Token 발급

- **발급**: 카카오 인증 성공 + 회원 매핑 완료 시.
- **Access**: JWT, Bearer header로 전달. 클라이언트는 메모리에 보관.
- **Refresh**: HttpOnly + Secure + SameSite=Lax cookie. Path는 `/api/v2/auth/refresh`로 제한(가능 시).

### 8.3 만료/재발급 정책

| 토큰 | 만료 | 재발급 |
|------|------|--------|
| Access | 15분(환경변수) | `POST /api/v2/auth/refresh` 호출 시 |
| Refresh | 14일(환경변수) | 사용 시 Rotation으로 새 Refresh 발급 |

### 8.4 저장 위치

| 위치 | Access | Refresh |
|------|--------|---------|
| 클라이언트 | 메모리(React state 등) | HttpOnly cookie(JavaScript 접근 불가) |
| 서버 | 발급 시에만 생성, 별도 저장 없음 | Redis/DB에 jti, owner_id, expires_at 저장 |

---

## 9. 보안 설계

### 9.1 state 파라미터

- OAuth 요청 시 cryptographically random state 생성(UUID v4 등).
- 콜백 시 state 검증. 불일치/미존재/만료 시 400 에러.

### 9.2 CSRF 대응

- Refresh/Logout 등 cookie 기반 엔드포인트: `Origin`/`Referer` allowlist 검증(`https://everbit.kr`).
- SameSite=Lax로 같은 사이트 요청에서만 cookie 전송.

### 9.3 Redirect URI 검증

- 카카오 개발자 콘솔에 등록된 Redirect URI와 요청 `redirect_uri`가 정확히 일치해야 한다.
- 로컬: `http://localhost:8080/api/v2/auth/callback` 등. 운영: `https://api.everbit.kr/api/v2/auth/callback`.

### 9.4 토큰 노출 방지

- Access Token은 응답 Body에 넣을 수 있으나, URL fragment 등 노출 경로를 최소화한다.
- Refresh는 절대 Body/URL에 포함하지 않는다.
- localStorage/sessionStorage에 Refresh 저장 금지.

### 9.5 로그 마스킹

- 토큰, 키, 쿠키 값, 암호문은 로그에 기록하지 않는다.
- `traceId`, `owner_id`, `event_type` 등만 로깅.

### 9.6 회원 식별키 관리

- 외부 노출: `public_id`(UUID v7). 내부: `id`(bigint).
- JWT `sub`에는 `public_id` 또는 `id` 사용. 일관되게 하나로 고정.

### 9.7 계정 연동 탈취 방지

- Refresh Rotation + 재사용 탐지로 탈취된 Refresh의 유효 기간을 최소화.
- 재사용 감지 시 해당 grant 전체 무효화.

---

## 10. 예외/에러 처리

| 상황 | HTTP | code | 처리 |
|------|------|------|------|
| 인가 코드 누락 | 400 | BAD_REQUEST | "인증 코드가 없습니다." |
| state 불일치/만료 | 400 | INVALID_STATE | "유효하지 않은 요청입니다." |
| 토큰 교환 실패 | 502 | KAKAO_TOKEN_ERROR | 카카오 오류 전달 또는 "일시적 오류" |
| 사용자 정보 조회 실패 | 502 | KAKAO_USER_ERROR | 동일 |
| 카카오 이메일 미제공 | 200 | (v2 MVP) 이메일 nullable 허용. P1에서 필수화 시 별도 플로우 |
| OWNER 존재 + 다른 계정 | 403 | NOT_OWNER | "등록된 계정만 사용할 수 있습니다." |
| Refresh 재사용 탐지 | 401 | REFRESH_REUSE_DETECTED | 해당 grant 무효화, 재로그인 유도 |
| 탈퇴 회원 재가입 | 정책 의존 | - | v2: 신규 생성. 기존 데이터는 별도 정책 |
| 연동 해제 계정 | - | - | 카카오 연동 해제는 everbit 탈퇴와 무관. 재로그인 시 동일 kakao_id로 인증 |

---

## 11. 데이터 모델 제안

### 11.1 app_user (기존)

| Column | Type | Notes |
|--------|------|-------|
| id | bigint PK | |
| public_id | uuid unique | 외부 노출 |
| kakao_id | varchar unique | 카카오 식별자 |
| email | varchar nullable | |
| created_at | timestamptz | |
| updated_at | timestamptz | |

### 11.2 SocialAccount (선택, v2 MVP에서는 생략)

- v2는 카카오 단일이므로 `app_user.kakao_id`로 충분.
- 다중 소셜 연동 시 `social_account(provider, provider_user_id, user_id)` 테이블 도입.

### 11.3 RefreshToken / RefreshSession

| Column | Type | Notes |
|--------|------|-------|
| jti | varchar PK | Refresh Token의 고유 식별자 |
| owner_id | bigint FK | app_user.id |
| expires_at | timestamptz | |
| created_at | timestamptz | |

- Redis 사용 시: `refresh:{jti}` → `{owner_id, expires_at}` (TTL 설정)
- DB 사용 시: `refresh_session` 테이블, 주기적 만료 row 정리

### 11.4 감사 로그(Audit)

| 이벤트 | 기록 항목 |
|--------|-----------|
| LOGIN_SUCCESS | owner_id, trace_id, ip_hash(선택) |
| LOGIN_REJECTED_NOT_OWNER | kakao_id_hash, trace_id |
| REFRESH_SUCCESS | owner_id, trace_id |
| REFRESH_REUSE_DETECTED | jti, owner_id, trace_id |
| LOGOUT | owner_id, trace_id |

---

## 12. API 설계

| Method | Path | 설명 |
|--------|------|------|
| GET | /api/v2/auth/start | OAuth 시작. state 생성 후 Kakao redirect URL로 302 |
| GET | /api/v2/auth/callback | OAuth 콜백. code 교환, 회원 매핑, 토큰 발급 |
| POST | /api/v2/auth/refresh | Refresh cookie로 Access 재발급. Rotation 적용 |
| POST | /api/v2/auth/logout | Refresh 폐기, cookie 만료 |
| DELETE | /api/v2/auth/me | 회원 탈퇴(선택, P1) |

### 12.1 인증 시작 API

**GET /api/v2/auth/start**

- Query: (없음)
- Response: 302 Redirect to `https://kauth.kakao.com/oauth/authorize?client_id=...&redirect_uri=...&response_type=code&state=...&scope=...`

### 12.2 콜백 API

**GET /api/v2/auth/callback?code=...&state=...**

- Query: `code`, `state` (필수). `error`, `error_description` (카카오 에러 시)
- Response 성공: 302 to `https://everbit.kr/dashboard` (또는 `?next=`) + Set-Cookie(Refresh) + Body에 Access 또는 별도 전달 방식
- Response 실패: 400/403/502 + 표준 에러 본문

### 12.3 로그인 완료 응답

- **Option A**: 302 Redirect + Set-Cookie(Refresh) + URL fragment `#access_token=...` (프론트가 fragment에서 추출)
- **Option B**: 302 Redirect + Set-Cookie(Refresh) + Set-Cookie(Access, 짧은 Path, HttpOnly 아님) — XSS 리스크 있음
- **권장**: Option A. Access는 fragment로 1회 전달, 프론트가 메모리에 저장. 또는 302 후 프론트가 `/api/v2/auth/me` 또는 전용 엔드포인트로 Access 요청(Refresh cookie 자동 전송).

**추정 보완**: 최종 전달 방식은 `docs/api/contracts.md`에 반영 후 고정.

### 12.4 로그아웃 API

**POST /api/v2/auth/logout**

- Headers: `Authorization: Bearer <access>` (선택, 있으면 owner 검증). Cookie: Refresh
- 동작: Refresh jti 무효화, Set-Cookie로 Refresh cookie 만료(Expires=0, Max-Age=0)
- Response: 204 No Content

### 12.5 토큰 재발급 API

**POST /api/v2/auth/refresh**

- Headers: Cookie에 Refresh
- 동작: jti 검증, 재사용 탐지, Rotation(새 Refresh 발급, 기존 무효화), 새 Access 발급
- Response: 200 + Body `{ "accessToken": "...", "expiresIn": 900 }` + Set-Cookie(새 Refresh)

### 12.6 회원 탈퇴/연동 해제 API

- **회원 탈퇴**: P1. everbit 계정 삭제. 카카오 연동 해제는 카카오 개발자 콘솔/사용자 설정에서 수행.
- **연동 해제**: everbit가 카카오 연동 해제를 API로 호출하는 기능은 v2 MVP 제외.

---

## 13. 프론트엔드 구현 가이드

### 13.1 로그인 버튼 클릭

```tsx
// Next.js
const handleLogin = () => {
  window.location.href = `${API_BASE}/api/v2/auth/start`;
};
```

### 13.2 카카오 redirect 처리

- 콜백 URL이 백엔드(`api.everbit.kr`)이므로, 카카오 인증 완료 후 사용자는 `api.everbit.kr/api/v2/auth/callback`으로 이동.
- 백엔드가 처리 후 `everbit.kr`로 302 Redirect. 이때 Access Token 전달 방식에 따라:
  - Fragment: `everbit.kr/dashboard#access_token=...` → `useEffect`에서 `window.location.hash` 파싱
  - 또는 백엔드가 `everbit.kr/auth/complete?access_token=...` 등으로 redirect(보안상 POST 또는 짧은 유효기간 권장)

### 13.3 인증 완료 후 상태 저장

```tsx
// Zustand 등
const useAuthStore = create((set) => ({
  accessToken: null,
  setAccessToken: (token) => set({ accessToken: token }),
  clearAuth: () => set({ accessToken: null }),
}));
```

### 13.4 보호 라우트 처리

```tsx
// middleware 또는 HOC
if (!accessToken) {
  return redirect('/login');
}
```

### 13.5 로그인 실패 UX

- 403: "이 서비스는 등록된 계정만 사용할 수 있습니다."
- 502: "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."
- 401(Refresh 실패): "세션이 만료되었습니다. 다시 로그인해 주세요." → `/login`

---

## 14. 백엔드 구현 가이드

### 14.1 카카오 토큰 요청

```java
// Spring WebClient
MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
body.add("grant_type", "authorization_code");
body.add("client_id", kakaoClientId);
body.add("redirect_uri", redirectUri);
body.add("code", code);
body.add("client_secret", kakaoClientSecret);

// POST /oauth/token
```

### 14.2 사용자 정보 조회

```java
// GET /v2/user/me
// Header: Authorization: Bearer {kakao_access_token}
```

### 14.3 회원 매핑 로직

```java
Optional<AppUser> user = appUserRepository.findByKakaoId(kakaoId);
if (user.isEmpty()) {
  if (appUserRepository.existsAny()) {
    throw new NotOwnerException(); // 403
  }
  user = Optional.of(appUserRepository.save(new AppUser(kakaoId, email, ...)));
}
```

### 14.4 신규 회원 처리

- OWNER가 없으면 신규 생성. 있으면 403.

### 14.5 JWT/세션 발급

- Access: JWT 생성, `sub=owner_id`, `exp=now+15min`
- Refresh: UUID 생성, jti로 Redis/DB 저장, HttpOnly cookie로 전달

---

## 15. 시퀀스 다이어그램

```
User          Next.js         Spring Boot     Kakao
 |                |                  |           |
 |-- 로그인 클릭 ->|                  |           |
 |                |-- GET /auth/start ->|          |
 |                |                  |-- state 저장
 |                |<- 302 redirect ---|          |
 |<- redirect ----|                  |           |
 |                |                  |           |
 |-- Kakao login page -------------------------->|
 |<- redirect + code ----------------------------|
 |                |                  |           |
 |-- GET /callback?code= ----------------------->|
 |                |                  |-- token 교환
 |                |                  |---------->|
 |                |                  |<- tokens -|
 |                |                  |-- /user/me
 |                |                  |---------->|
 |                |                  |<- user info
 |                |                  |-- 회원 매핑
 |                |                  |-- Access+Refresh 발급
 |                |<- 302 + Set-Cookie + Access -|
 |<- dashboard ---|                  |           |
 |                |                  |           |
 |-- API 요청 (Bearer) ------------>|           |
 |                |                  |-- JWT 검증
 |<- 200 ---------|                  |           |
 |                |                  |           |
 |-- 401 (만료) ---|                  |           |
 |                |-- POST /refresh (Cookie) ---->|
 |                |                  |-- Rotation
 |                |<- 200 + new Access + Cookie --|
 |                |-- 원 요청 재시도 ------------->|
 |<- 200 ---------|                  |           |
```

---

## 16. 의사결정 포인트

| 항목 | 결정 | 근거 |
|------|------|------|
| BFF 사용 여부 | 사용하지 않는다(v2 MVP) | Next.js가 API 직접 호출. 구조 단순화. |
| 프론트 직접 토큰 처리 | 카카오 토큰은 금지. 서비스 Access만 메모리 보관 | 카카오 토큰 노출·오용 방지. |
| JWT vs 세션 | Access는 JWT, Refresh는 서버 저장 | Stateless API, 수평 확장 용이. Refresh는 탈취 대응을 위해 서버 관리. |
| 카카오 이메일 신뢰 범위 | v2 MVP에서 nullable. 필요 시 추후 필수화 | 카카오 이메일 동의 선택 시 미제공 가능. |

---

## 17. 오픈 이슈 / 추가 검토 항목

| 이슈 | 상태 |
|------|------|
| Access Token 전달 방식(302 후 fragment vs 별도 API) | `docs/api/contracts.md`에 확정 후 반영 |
| Redis vs DB for Refresh jti | 운영 환경에 따라. ADR-0007은 Redis 전제. VM에 Redis 없을 시 DB 또는 외부 Redis |
| Reuse Detection Window | 1인 전용이라 동시 요청 빈도 낮음. 단순 즉시 무효화로 충분할 수 있음 |
| 회원 탈퇴 시 데이터 보존 기간 | P1에서 정책 확정 |

---

## 18. 최종 권장안

- **인증/인가 책임**: 백엔드가 전담. 클라이언트는 토큰 전달·보관만.
- **카카오 토큰**: 서버에서만 사용 후 폐기. 클라이언트에 노출 금지.
- **서비스 토큰**: Access(JWT, Bearer) + Refresh(HttpOnly cookie, Rotation).
- **OWNER 락**: 최초 로그인 계정만 허용.
- **보안**: state, Origin/Referer 검증, 로그 마스킹.

---

## 부록 C. 권장 아키텍처 요약

```
[클라이언트]
- 카카오 토큰: 보관하지 않음
- 서비스 Access: 메모리
- 서비스 Refresh: HttpOnly cookie (자동 전송)

[백엔드]
- OAuth 시작/콜백 처리
- 카카오 API 직접 호출 (토큰 교환, /user/me)
- 회원 매핑 (kakao_id → app_user)
- Access JWT + Refresh(jti) 발급
- Refresh Rotation + 재사용 탐지
- 로그아웃 시 jti 무효화 + cookie 만료

[저장소]
- Redis/DB: refresh_session (jti, owner_id, expires_at)
- state: Redis 또는 세션 (10분 TTL)
```

---

## 부록 D. 구현 체크리스트

### 인증 플로우
- [ ] GET /api/v2/auth/start 구현 (state 생성, Kakao redirect)
- [ ] GET /api/v2/auth/callback 구현 (code 교환, user info, 회원 매핑)
- [ ] Access JWT 발급
- [ ] Refresh 생성 및 jti 저장
- [ ] Set-Cookie(Refresh) + 302 응답

### 토큰 관리
- [ ] POST /api/v2/auth/refresh (Rotation, 재사용 탐지)
- [ ] POST /api/v2/auth/logout (jti 무효화, cookie 만료)
- [ ] Refresh cookie: HttpOnly, Secure, SameSite=Lax
- [ ] Refresh cookie Path 제한 (가능 시)

### 보안
- [ ] state 검증
- [ ] Origin/Referer allowlist (refresh, logout)
- [ ] Redirect URI 검증
- [ ] 로그 마스킹 (토큰/키/쿠키)

### OWNER 락
- [ ] 최초 로그인 → OWNER 생성
- [ ] OWNER 존재 시 다른 kakao_id → 403

### 프론트엔드
- [ ] 로그인 버튼 → /auth/start
- [ ] 콜백 후 Access 저장 (메모리)
- [ ] 401 시 refresh 1회 + 재시도
- [ ] 403 시 로그인 유도
- [ ] 보호 라우트 처리

### 테스트
- [ ] 로그인 성공 (신규/기존)
- [ ] OWNER 락 (403)
- [ ] Refresh Rotation
- [ ] Refresh 재사용 탐지
- [ ] Logout
