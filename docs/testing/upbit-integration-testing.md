# Upbit 연동 테스트 가이드

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-14 (Asia/Seoul)

목표:
- Upbit REST 연동(키 등록/검증/계정·주문 API)을 로컬·CI에서 안전하게 검증하는 방법을 정리한다.
- 실거래 전에 429/418/예외 처리·에러 응답 형식을 확인할 수 있도록 한다.

SoT:
- `docs/integrations/upbit.md` (연동 스펙·예외 정책)
- `docs/api/contracts.md` §11 (에러 본문·reasonCode)
- `docs/testing/strategy.md` (스텁 정책)

---

## 1. 테스트 유형 요약

| 유형 | 목적 | Upbit 호출 | 권장 환경 |
|------|------|------------|-----------|
| 단위/통합(WireMock) | 429/418/5xx·예외 핸들링 회귀 | 스텁 | CI |
| API 수동(실키) | 키 등록·상태·계정 조회 검증 | 실 Upbit | 로컬만 |
| E2E | 로그인 → 키 등록 → 대시보드 | 스텁 또는 실키 | 로컬/스테이징 |

---

## 2. 로컬에서 Upbit 키 API 테스트(실제 키)

**전제**
- 서버가 로컬에서 실행 중이다.
- 카카오 OAuth 로그인으로 발급된 Access Token이 있다.
- Upbit OpenAPI에서 발급한 Access Key / Secret Key가 있다(테스트용 권한만 권장).

**주의**
- 시크릿은 `.env.local` 또는 환경변수로만 주입하고, 터미널/로그에 평문 출력하지 않는다.
- SoT: `docs/security/secrets.md`, AGENTS.md §4.

### 2.1 키 등록 여부 확인 — GET /api/v2/upbit/key/status

```bash
curl -s -X GET "http://localhost:8080/api/v2/upbit/key/status" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**기대 응답**
- 키 미등록: `{"status":"NOT_REGISTERED","lastVerifiedAt":null,"verificationErrorCode":null}`
- 키 등록·검증 성공: `{"status":"REGISTERED","lastVerifiedAt":"2026-03-14T...","verificationErrorCode":null}`
- 키 등록됐으나 Upbit 검증 실패: `{"status":"VERIFICATION_FAILED","lastVerifiedAt":null,"verificationErrorCode":"RATE_LIMIT_429"}` 등 (reasonCode는 §12 참조)

### 2.2 키 등록 + 검증 — POST /api/v2/upbit/key

```bash
curl -s -X POST "http://localhost:8080/api/v2/upbit/key" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accessKey":"YOUR_UPBIT_ACCESS_KEY","secretKey":"YOUR_UPBIT_SECRET_KEY"}'
```

**기대 응답**
- 성공 시: `{"status":"REGISTERED","lastVerifiedAt":"...","verificationErrorCode":null}` (200)
- 검증 실패 시: 200 + `status: "VERIFICATION_FAILED"`, `verificationErrorCode`에 사유(예: `UPBIT_API_ERROR`, `RATE_LIMIT_429`, `UPBIT_BLOCKED_418`)
- 요청 검증 실패: 400 + 표준 에러 본문(code, message, reasonCode)
- 계정 없음: 404 + reasonCode `OWNER_NOT_FOUND`

### 2.3 키 폐기 — DELETE /api/v2/upbit/key

```bash
curl -s -X DELETE "http://localhost:8080/api/v2/upbit/key" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

**기대 응답**
- 204 No Content

---

## 3. 예외 처리·에러 응답 검증(리팩토링 후 구조)

Upbit 관련 예외는 **UpbitExceptionHandler**(`integrations/upbit`)에서만 처리한다.

- **UpbitApiException**
  - 429 → HTTP 429, `code: "RATE_LIMIT_EXCEEDED"`, `reasonCode: "RATE_LIMIT_429"`
  - 418 → HTTP 503, `code: "SERVICE_UNAVAILABLE"`, `reasonCode: "UPBIT_BLOCKED_418"`
  - 그 외 4xx/5xx → HTTP 502, `code: "UPBIT_ERROR"`, `reasonCode: "UPBIT_API_ERROR"`
- **UpbitException**(API가 아닌 연동 오류)
  - HTTP 502, `code: "UPBIT_ERROR"`, `reasonCode: "UPBIT_ERROR"`

키 등록/상태 API는 **검증 실패 시 예외를 던지지 않고** 200 + `status: "VERIFICATION_FAILED"` + `verificationErrorCode`로 반환하므로, 위 HTTP 429/503/502는 다른 경로(예: 추후 주문·잔고 API를 직접 호출하는 엔드포인트)에서 발생할 때 확인한다.  
회귀 테스트는 아래 WireMock으로 검증한다.

---

## 4. WireMock으로 429/418/5xx 회귀 테스트(권장)

개발/CI에서는 실 Upbit를 호출하지 않고, Upbit REST를 WireMock으로 스텁한다.

**위치**
- 서버 테스트: `server/src/test/.../integrations/upbit/` (또는 기존 통합 테스트 패키지)
- Upbit Base URL을 테스트 프로파일에서 `http://localhost:${wiremock.port}/` 등으로 치환

**필수 시나리오**
1. **429** — Upbit가 429 반환 시 클라이언트가 `UpbitApiException`(is429=true)을 던지고, `UpbitExceptionHandler`가 429 + `RATE_LIMIT_429`로 응답하는지.
2. **418** — Upbit가 418 반환 시 503 + `UPBIT_BLOCKED_418`로 응답하는지.
3. **5xx/기타 4xx** — 502 + `UPBIT_API_ERROR`로 응답하는지.
4. **UpbitException**(네트워크/파싱 등) — 502 + `UPBIT_ERROR`로 응답하는지.

테스트 템플릿은 `docs/testing/backend-tdd-template.md` 및 `docs/testing/strategy.md` §4 스텁 정책을 따른다.

---

## 5. 로컬 실행 전 체크리스트

- [ ] `UPBIT_KEY_MASTER_KEY`(또는 프로젝트에서 사용하는 마스터키 환경변수)가 설정되어 있어 키 암복호화가 동작하는가?
- [ ] `application.yml`/프로파일에서 Upbit base URL이 실 서버(`https://api.upbit.com`) 또는 테스트용 스텁 URL로 올바르게 설정되어 있는가?
- [ ] Access Token은 카카오 로그인 또는 테스트용 발급 경로로 얻었는가?
- [ ] Upbit 키는 OpenAPI에서 발급했고, 허용 IP(로컬이면 필요 시 0.0.0.0/0 테스트용)와 권한(자산조회/주문 등)이 맞는가?

---

## 6. 요약

- **로컬 수동 테스트**: 위 curl로 `/api/v2/upbit/key`의 status/register/delete를 실 Upbit 키로 검증.
- **예외 구조**: Upbit 예외는 `UpbitExceptionHandler`에서만 처리하며, 429/418/502와 reasonCode가 계약(§11, §12)과 일치하는지 확인.
- **CI 회귀**: WireMock으로 429/418/5xx 및 일반 UpbitException 시나리오를 자동 테스트하여, 예외 리팩토링 후에도 동작이 유지되는지 검증.

추가 시나리오(주문 생성/취소/조회)는 주문 파이프라인 테스트와 함께 `docs/architecture/order-pipeline.md`, `docs/testing/strategy.md`를 참고하여 확장한다.
