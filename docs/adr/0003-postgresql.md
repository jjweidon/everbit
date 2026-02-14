# ADR 0003: PostgreSQL

## 상태

수락됨

## 배경

v2 데이터 영속성 및 트랜잭션 지원을 위한 관계형 DB 선택이 필요하다.

## 결정

* **PostgreSQL**을 메인 DB로 채택
* OCI Always Free 등에서 관리형 Postgres 또는 Self-host 선택 가능

## 결과

* ACID 트랜잭션, JSON 지원, 확장성 확보
* Spring Data JPA / R2DBC 등과 호환
