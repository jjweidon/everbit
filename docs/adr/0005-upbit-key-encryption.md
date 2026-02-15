# ADR-0005: Upbit API 키 암호화 저장(AES-GCM) 및 키관리

Status: **Accepted**  
Date: 2026-02-14

---

## Context
Upbit API 키는 유출 시 즉시 실거래 피해로 이어질 수 있는 최상위 민감정보다.  
자동매매를 수행하려면 키 저장이 필요하므로, **평문 저장을 금지**하고 안전한 암호화 저장이 필요하다.

---

## Decision
- Upbit Access/Secret Key는 DB에 **암호문으로만** 저장한다.
- 암호화는 **AES-256-GCM**(인증 암호화)을 사용한다.
- 암호화 마스터키는 `UPBIT_KEY_MASTER_KEY`로 명명하고,
  - 로컬: `.env.local`에서 주입(커밋 금지)
  - 운영: VM 환경변수(또는 `/etc/everbit/everbit.env`)에서 주입한다.
- 키 로테이션을 위해 `key_version`을 유지한다.

---

## Consequences
- 키 유출 위험이 크게 감소한다(단, 마스터키 유출 시 동일하게 위험).
- 로테이션 절차가 필요하다.
- 구현 복잡도가 증가하지만 v2 MVP에 포함한다.

---

## Alternatives
- 키 저장 금지(자동매매 상시 실행과 충돌)
- 단순 Base64/마스킹(암호화가 아니므로 불가)
