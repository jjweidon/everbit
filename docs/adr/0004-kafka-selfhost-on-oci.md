# ADR 0004: Kafka Self-host on OCI

## 상태

수락됨

## 배경

* 관리형 Kafka(Confluent Cloud, CloudKarafka, Aiven 등)는 월 $95~$385 수준으로, MVP 예산 $8 미만에 부적합.
* Upstash는 사용량 기반이지만 트레이딩 이벤트 양에 따라 $8 초과 위험.

## 결정

* **OCI 단일 VM에 Kafka Self-host (KRaft 단일 노드)** 채택
* Always Free Tier(Ampere A1) 기준 추가 비용 $0
* Docker로 Kafka 구동

## 결과

* MVP 예산 내 Kafka 운영 가능
* 추후 운영 부담이 커지면 Upstash 등 관리형으로 교체 검토(별도 ADR)
