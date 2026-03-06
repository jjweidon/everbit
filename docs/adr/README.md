# ADR (Architecture Decision Records) 목록

Status: **Active**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

아키텍처 결정은 이 디렉터리의 ADR로 고정한다.  
파일명 규칙: `NNNN-title.md` (4자리 번호 + kebab-case).

---

## 목록

| 번호 | 제목 | 요약 |
|------|------|------|
| [0001](./0001-git-strategy.md) | Git 브랜치/커밋 전략 | v1/v2 브랜치, SoT는 v2, 문서 우선 |
| [0002](./0002-single-tenant.md) | 싱글 테넌트 | 1인 전용(OWNER 단일) |
| [0003](./0003-postgresql.md) | PostgreSQL | DB 표준, 마이그레이션 도구 |
| [0004](./0004-kafka-selfhost-on-oci.md) | Kafka self-host on OCI | v2 MVP에서 제외, Outbox로 대체 |
| [0005](./0005-upbit-key-encryption.md) | Upbit 키 암호화 | DB 암호문 저장, AES-GCM |
| [0006](./0006-admin-surface-access.md) | Admin Surface 접근 | Grafana/Jenkins 등 기본 비공개, SSH 터널 |
| [0007](./0007-auth-session.md) | 인증/세션 | Access Token(Bearer) + Refresh(HttpOnly 쿠키) |
| [0008](./0008-web-push-notification.md) | Web Push 알림 | OrderAccepted 트리거, best effort |
| [0009](./0009-postgres-outbox-queue-v2-mvp.md) | Postgres Outbox/Queue | v2 MVP에서 Kafka 대신 outbox_event |

---

## 참고

- 새 ADR 작성 시: `docs/AGENTS.md` §2 ADR 규칙 준수.
- 결정 계층: `docs/README.md` 권장 읽는 순서 참조.
