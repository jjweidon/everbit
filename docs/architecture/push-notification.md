# Push Notifications (Web Push) 상세 스펙

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- 주문 접수(Upbit ACK) 시 클라이언트에 **Web Push**로 즉시 알림을 보낸다. (FR-NOTI-001)
- 구현/운영에서 흔히 깨지는 지점을 **규격으로 고정**한다:
  - Service Worker 등록 흐름
  - 구독(Subscription) 등록/해지 API
  - 알림 Payload 스키마
  - 알림 클릭 시 딥링크 규격
  - 실패 코드별 구독 정리(청소) 규칙

비목표:
- 모바일 네이티브 앱(FCM/APNS) 지원
- 푸시 자체를 거래 SoT로 사용(푸시는 best effort)

연관 문서:
- 기능 요구사항: `docs/requirements/functional.md` (FR-NOTI-001~003)
- 시크릿: `docs/security/secrets.md` (VAPID_PRIVATE_KEY)
- 이벤트/토픽: `docs/architecture/kafka-topics.md` (OrderAccepted 이벤트)
- 주문 파이프라인: `docs/architecture/order-pipeline.md` (OrderAccepted 정의)

---

## 1. 핵심 결정(고정)

### 1.1 트리거 이벤트
- **OrderAccepted** 이벤트 발생 시 푸시 발송
- 의미: Upbit 주문 생성이 성공하여 **Upbit UUID(주문 uuid)** 를 확보한 시점(ACK)

### 1.2 알림 목적
- 사용자(OWNER)가 실거래 주문이 접수된 사실을 즉시 인지
- 대시보드에서 해당 주문 상세로 빠르게 이동

### 1.3 푸시 전달 보장 수준
- **best effort**
- 실패/지연이 있어도 거래 흐름은 계속 진행해야 한다.
- 중복 전송 가능성을 전제로 하고, payload에 `eventId`를 포함한다.

---

## 2. Client: Service Worker 등록 & 권한 UX 흐름

원칙:
- 권한 요청은 **사용자 명시적 액션**에서만 수행한다(첫 진입 강제 요청 금지).
- 권한 거부/차단 상태를 UI로 명확히 안내한다.

### 2.1 사용자 액션 기반 흐름(권장)
1) 사용자가 UI에서 “푸시 알림 사용” 토글 ON
2) 브라우저가 `Notification.permission` 확인
   - `default`: 권한 요청 → 사용자가 허용하면 계속
   - `granted`: 계속
   - `denied`: “브라우저 설정에서 허용 필요” 안내(바로 종료)
3) Service Worker 등록
4) Push subscription 생성
5) 서버에 구독 등록 API 호출
6) 성공 시 UI에 “활성화됨” 표시

### 2.2 Service Worker 파일 위치(권장)
- Next.js: `public/sw.js` (또는 `public/push-sw.js`)
- 등록 경로는 앱에서 고정한다.
- scope 기본 `/` 권장(전체 앱 알림 처리).

### 2.3 권한 상태별 UI 규격(최소)
- `granted`: “푸시 사용 중” + “해지 버튼”
- `default`: “푸시 사용하기” (권한 요청은 토글/버튼 클릭 시 수행)
- `denied`: “브라우저 설정에서 알림을 허용해야 함” 가이드(링크/설명)

---

## 3. Client: Subscription 생성 규격

### 3.1 VAPID 공개키 전달
- 클라이언트는 `NEXT_PUBLIC_VAPID_PUBLIC_KEY`로 공개키를 받는다.
- 공개키는 base64url 형식으로 제공한다.

### 3.2 PushManager.subscribe 옵션(고정)
- `userVisibleOnly: true`
- `applicationServerKey: <VAPID_PUBLIC_KEY Uint8Array>`

### 3.3 구독 객체 정규화(서버 전달 필드)
브라우저의 subscription(JSON)에서 서버로 전송할 최소 필드:
- `endpoint`
- `keys.p256dh`
- `keys.auth`
- (옵션) `userAgent` (디버깅/정리용)

---

## 4. Server API: 구독 등록/해지/조회

인증:
- 모든 API는 OWNER-only
- Access Token(Bearer) 기반
- Refresh는 HttpOnly cookie(세션 전략은 ADR-0007)

Base URL 예시:
- `/api/v2/push/...`

### 4.1 POST `/api/v2/push/subscriptions` (등록/갱신)

의도:
- “등록”과 “갱신”을 동일 엔드포인트로 처리한다.
- 동일 endpoint가 이미 존재하면 update(upsert)한다.

Request DTO (JSON):
```json
{
  "endpoint": "https://example.pushservice.com/...",
  "p256dh": "base64...",
  "auth": "base64...",
  "userAgent": "Mozilla/5.0 ...",
  "deviceTag": "optional-string"
}
```

Validation(필수):
- endpoint, p256dh, auth: not blank
- 길이 제한(권장):
  - endpoint: <= 2048
  - p256dh/auth: <= 512
  - userAgent: <= 512
  - deviceTag: <= 64

Response DTO:
```json
{
  "id": 123,
  "enabled": true,
  "createdAt": "2026-02-15T00:00:00.000Z",
  "updatedAt": "2026-02-15T00:00:00.000Z"
}
```

DB 규칙:
- UNIQUE(owner_id, endpoint)
- 등록 시 `enabled=true`로 강제

멱등:
- 같은 endpoint 재등록은 upsert로 처리(중복 row 금지)

### 4.2 GET `/api/v2/push/subscriptions` (목록)

Response DTO:
```json
{
  "items": [
    {
      "id": 123,
      "endpoint": "https://example.pushservice.com/...",
      "enabled": true,
      "createdAt": "2026-02-15T00:00:00.000Z",
      "updatedAt": "2026-02-15T00:00:00.000Z",
      "userAgent": "Mozilla/5.0 ..."
    }
  ]
}
```

보안:
- endpoint는 개인 식별/추적 가능성이 있으므로 UI에 전부 노출할 필요는 없다.
- MVP에서는 디버깅용으로만 제한적으로 보여준다(마스킹 권장).

### 4.3 DELETE `/api/v2/push/subscriptions/{id}` (해지)

Response:
```json
{
  "id": 123,
  "enabled": false
}
```

서버 동작(결정 고정):
- delete(물리 삭제) 대신 기본은 `enabled=false`로 비활성화(감사/분석 용이)
- 필요 시 GC 배치로 오래된 disabled row 정리(P1)

### 4.4 POST `/api/v2/push/test` (테스트 발송, P0 권장)
운영 초기 검증에 필요하다.

Request:
```json
{
  "message": "test message",
  "deepLink": "/dashboard?tab=orders"
}
```

Response:
```json
{
  "requested": 2,
  "delivered": 1,
  "failed": 1
}
```

---

## 5. 알림 Payload 스키마(고정)

### 5.1 공통 Envelope
- `eventId`는 중복 전송 방지/추적용
- `type`으로 알림 종류 확장 가능

```json
{
  "schemaVersion": 1,
  "eventId": "uuid",
  "type": "ORDER_ACCEPTED",
  "occurredAt": "2026-02-15T00:00:00.000Z",
  "title": "주문 접수",
  "body": "KRW-BTC 매수 주문이 접수되었습니다.",
  "deepLink": "/orders/UPBIT_UUID",
  "data": {}
}
```

### 5.2 ORDER_ACCEPTED payload (최소 필드)
```json
{
  "schemaVersion": 1,
  "eventId": "uuid",
  "type": "ORDER_ACCEPTED",
  "occurredAt": "2026-02-15T00:00:00.000Z",
  "title": "주문 접수",
  "body": "KRW-BTC 매수 (ENTRY) 접수",
  "deepLink": "/orders/7f2c...upbitUuid",
  "data": {
    "strategyKey": "EXTREME_FLIP",
    "market": "KRW-BTC",
    "side": "BUY",
    "intentType": "ENTRY",
    "requestedKrw": 10000,
    "requestedVolume": null,
    "upbitUuid": "7f2c...upbitUuid"
  }
}
```

필드 규칙:
- `side`: 내부 표준(BUY/SELL)
- `intentType`: ENTRY/EXIT_STOPLOSS/EXIT_TP/EXIT_TRAIL/EXIT_TIME
- `requestedKrw` 또는 `requestedVolume`은 주문 타입에 따라 하나만 채워도 됨
- UI 표시를 위해 `title/body`는 서버에서 완성해서 보내도 되고,
  클라이언트가 `data`를 기반으로 재구성해도 된다(단, MVP에서는 서버 완성 권장).

---

## 6. 딥링크 규격(알림 클릭 동작)

원칙:
- 딥링크는 프론트 라우트 기준의 **상대 경로**로만 허용한다.
- 외부 URL/스킴 실행은 금지(피싱/오픈리다이렉트 방지).

### 6.1 deepLink 허용 규칙(고정)
- 반드시 `/`로 시작
- `//`(프로토콜 상대 URL) 금지
- `http:` `https:` 포함 금지
- 길이 제한(권장): 512

예:
- ✅ `/orders/{upbitUuid}`
- ✅ `/dashboard?tab=orders`
- ❌ `https://evil.com/...`
- ❌ `//evil.com/...`

### 6.2 Service Worker 클릭 처리 규격
- 알림 클릭 시:
  1) 열린 클라이언트 탭 중 everbit.kr이 있으면 focus
  2) 없으면 새 탭을 열고 deepLink로 이동
- deepLink는 `notification.data.deepLink`에서 읽는다.

---

## 7. Server: 발송 파이프라인(권장 구현)

### 7.1 트리거
- `OrderAccepted` 도메인 이벤트 발생
- `notification-worker`가 consume

### 7.2 구독 조회
- `push_subscription`에서 `enabled=true`인 모든 row 조회(싱글 테넌트지만 multi-device 허용)

### 7.3 발송
- WebPush 라이브러리로 각 subscription endpoint에 POST
- 발송 시 payload는 JSON string

### 7.4 결과 처리(필수)
- 성공: (선택) notification_log 기록
- 실패: 실패 코드별 정리 규칙 수행(아래 8장)

---

## 8. 실패 코드별 구독 정리 규칙(강제)

푸시는 실패가 정상이며, 방치하면 다음 문제가 생긴다.
- 매번 실패 호출로 리소스/시간 낭비
- 일부 브라우저에서 410/404가 지속되어 로그 오염

정리 정책은 **보수적으로** 간다:
- “구독이 더 이상 유효하지 않다”가 확실한 경우만 비활성화/삭제
- 네트워크/일시 장애는 재시도(또는 다음 이벤트에서 재시도)

### 8.1 Web Push 실패 유형 분류

A) **구독 만료/해지(영구 실패)** → 즉시 비활성화/삭제 대상  
- HTTP 404 Not Found
- HTTP 410 Gone  
의미: 해당 endpoint는 더 이상 유효하지 않음

조치(강제):
- 해당 endpoint의 subscription을 `enabled=false`로 전환
- (선택) disabled_at 기록
- (선택) notification_log에 failure reason 기록

B) **일시 장애(재시도 가능)** → 비활성화 금지  
- HTTP 429 Too Many Requests
- HTTP 500/502/503/504
- 네트워크 타임아웃

조치(권장):
- 즉시 재시도는 금지(폭주 방지)
- “다음 알림 이벤트”에서 자연 재시도(싱글 사용자라 충분)
- 필요하면 `next_retry_at` 필드 도입(P1)

C) **클라이언트/서버 구성 오류(개발 이슈)** → 즉시 분석  
- HTTP 400/401/403 등
- payload 스키마/키 문제 가능

조치(강제):
- subscription을 바로 비활성화하지 않는다(구독이 문제인지 확정 불가)
- 에러 로그에 correlation id + endpoint hash(원문 endpoint는 최소화) 남김
- 운영자가 원인 수정 후 재시도

### 8.2 endpoint 해시 로그 규칙(권장)
- endpoint 원문을 로그에 남기지 말고 해시(예: SHA-256 8자리)로 남긴다.
- 이유: endpoint는 사용자 환경 식별 가능.

---

## 9. 보안/시크릿(강제)

- `VAPID_PRIVATE_KEY`는 운영 시크릿이며:
  - Git 커밋 금지
  - 로그 출력 금지
  - `.cursorignore`로 인덱싱 제외
- `VAPID_PUBLIC_KEY`는 클라이언트에 노출 가능
- deepLink는 상대 경로만 허용(오픈 리다이렉트 방지)

---

## 10. 테스트 요구사항(최소)

### 10.1 Backend 통합 테스트(필수)
- 구독 등록 upsert(동일 endpoint)
- 전송 성공 시 delivered count 반영
- 410/404 실패 시 subscription 비활성화
- 500/timeout 시 비활성화하지 않음

### 10.2 Frontend(필수)
- 권한 상태별 UI 분기
- 토글 ON 시 SW 등록 + 구독 생성 + 서버 등록 호출
- 토글 OFF 시 서버 해지 호출

### 10.3 E2E(권장)
- OrderAccepted 발생(스텁) → 서버가 push 전송 시도까지 확인(실제 OS 알림 수신은 환경 의존)
- 최소는 “push send 함수 호출/응답”을 검증하는 수준으로 고정

---

## 11. 운영 체크리스트

- [ ] `NEXT_PUBLIC_VAPID_PUBLIC_KEY`(Vercel) 설정 완료
- [ ] `VAPID_PRIVATE_KEY`(VM env) 설정 완료
- [ ] 구독 등록/해지 API OWNER-only 확인
- [ ] 410/404 구독 정리 동작 확인
- [ ] 딥링크 상대 경로 검증 적용
