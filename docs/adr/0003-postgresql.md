# ADR-0003: PostgreSQL 채택

- Status: **Accepted**
- Date: 2026-02-14

## Context
v2는 트레이딩 이벤트/백테스트 결과/전략 설정/관측 데이터를 저장한다.  
향후 Spring AI/RAG 도입 가능성이 있으며, 그 경우 PostgreSQL 생태계(예: 확장/벡터 저장) 활용 가능성을 고려한다.

## Decision
- 메인 RDBMS는 **PostgreSQL**을 사용한다.
- DB 마이그레이션 도구는 Flyway 또는 Liquibase 중 하나를 선정(별도 ADR 가능).
- 성능 핵심 테이블(주문/체결/시계열)은 인덱스/파티셔닝 전략을 문서화한다.

## Consequences
- MySQL 대비 운영 경험이 적을 수 있으나, 학습/운영을 감수하고 v2에서 일원화한다.
- RAG/벡터 확장 도입 시 변경 비용을 줄일 수 있다.

## Alternatives Considered
1) MySQL 유지  
- 장점: 기존 경험/일부 구현 재사용  
- 단점: 향후 RAG/확장 고려 시 재검토 가능성이 높음
