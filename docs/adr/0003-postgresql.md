# ADR-0003: PostgreSQL 채택

Status: **Accepted**  
Date: 2026-02-14

---

## Context
v2는 트레이딩 이벤트/백테스트 결과/전략 설정/관측 데이터를 저장한다.  
향후 AI/RAG 도입 가능성을 고려하면 PostgreSQL 생태계 활용 여지가 있다.

---

## Decision
- 메인 RDBMS는 **PostgreSQL**을 사용한다.
- 스키마 변경은 마이그레이션 도구로 관리한다(Flyway/Liquibase 중 1개를 코드 착수 전에 고정).
- 주문/체결 등 핵심 테이블은 인덱스/유니크 제약을 문서로 고정한다(`docs/architecture/data-model.md`).

---

## Consequences
- MySQL 대비 운영 경험이 부족할 수 있으나 v2에서는 PostgreSQL로 일원화한다.
- 확장(예: 벡터/고급 인덱스) 도입 시 변경 비용을 줄일 수 있다.

---

## Alternatives
- MySQL 유지(기존 경험 장점, 확장/기능 고려 시 재검토 가능성 높음)
