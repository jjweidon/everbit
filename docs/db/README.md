# DB 스키마/마이그레이션

Status: **Draft (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

이 디렉터리는 v2 MVP의 PostgreSQL 스키마 초안을 보관한다.

## 원칙
- 스키마 SoT는 `docs/architecture/data-model.md`다.
- 실행 가능한 DDL 초안은 `schema-v2-mvp.sql`이다.
- JPA 엔티티 매핑(복합키/공유PK)은 `docs/architecture/jpa-mapping.md`를 따른다.
- 마이그레이션 도구(Flyway/Liquibase)는 코드 착수 전에 1개로 고정한다(ADR-0003).

## 파일
- `schema-v2-mvp.sql`: v2 MVP 초기 스키마 DDL(초안)

## 주의
- 멱등/정합성을 보장하는 UNIQUE/PK/CHECK 제약은 “성능”이 아니라 “스펙”이다. 임의로 제거하지 않는다.
