# ADR-0008: Web Push 기반 주문 접수 알림

Status: **Accepted**  
Date: 2026-02-15

---

## Context
v2 MVP에서 “주문 접수(Upbit ACK) 시 즉시 인지”는 운영 안정성에 직접 영향을 준다.  
1인 프로젝트이므로 비용/운영 복잡도가 낮은 방식이 필요하다.

---

## Decision
- 푸시 알림은 **Web Push**를 사용한다.
- 클라이언트는 Service Worker + PushManager로 구독(subscription)을 생성하고 서버에 등록한다.
- 서버는 VAPID 키로 Web Push를 발송한다.
- 트리거:
  - `OrderAccepted` 이벤트 발생 시 push 발송(FRD: FR-NOTI-001)
- 푸시는 best effort이며 거래 SoT가 아니다.
- 전송 실패(구독 만료/해지)는 구독을 DB에서 비활성화/삭제한다.

---

## Consequences
- 모바일 앱 없이도 브라우저에서 알림 수신이 가능하다.
- 브라우저/OS 정책에 따라 수신률이 달라질 수 있다.
- VAPID private key는 운영 시크릿이며 엄격히 관리해야 한다.

---

## Alternatives
- FCM(운영/설정 복잡도 상승)
- 이메일/슬랙(Webhook) 알림(추가 계정/통합 필요)
