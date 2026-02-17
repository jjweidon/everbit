# 알림/경보(Alerting) 정책 (Grafana/Prometheus)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-15 (Asia/Seoul)

목표:
- “장애를 빨리 아는 것”보다 “손실/중복 주문을 먼저 막는 것”을 우선한다.
- 알림은 소음이 되지 않게 최소화하고, P0는 즉시 행동 가능한 형태로 고정한다.
- 단일 사용자(1인 운영) 환경에서 운영 가능하도록 단순하게 설계한다.

관련 문서(SoT):
- `docs/operations/admin-surface-policy.md` (Grafana/Prometheus 노출 정책)
- `docs/architecture/order-pipeline.md` (UNKNOWN/THROTTLED/SUSPENDED)
- `docs/integrations/upbit.md` (429/418/WS 정책)
- `docs/operations/runbook.md` (장애 조치 절차)

---

## 1. 알림 우선순위(Severity)

### 1.1 CRITICAL (P0)
정의:
- 손실 확대/중복 주문/자동매매 폭주/차단(418) 등 “즉시 중단”이 필요한 상태

행동:
- 즉시 Kill Switch OFF
- 원인 확인 후 수동 재개

### 1.2 WARNING (P1)
정의:
- 단기간에 장애로 발전 가능, 또는 성능/지연이 커지는 상태

### 1.3 INFO (P2)
정의:
- 관찰/개선 대상

---

## 2. 모니터링 구성(전제)

- Prometheus는 내부에서 scrape 한다.
- Grafana는 외부 노출하지 않는다(SSH 터널).
- 서버는 `/actuator/prometheus`를 제공한다.

권장 Exporter(v2 MVP 최소):
- node-exporter(호스트 CPU/MEM/DISK)
- spring actuator prometheus(서버 메트릭)

---

## 3. 커스텀 메트릭 계약(Backend 필수)

### 3.1 Upbit REST 상태
필수:
- `everbit_upbit_http_requests_total{endpoint,method,status}` (counter)
- `everbit_upbit_http_errors_total{endpoint,class}` (counter)
- `everbit_upbit_rate_limit_remaining{group}` (gauge)

### 3.2 주문 파이프라인 상태
필수:
- `everbit_order_attempt_total{status}` (counter)
- `everbit_market_state{market,state}` (gauge)
- `everbit_kill_switch_enabled{scope,strategyKey}` (gauge)

### 3.3 Outbox/Kafka 적체(가능하면)
권장:
- `everbit_outbox_pending` (gauge)
- `everbit_kafka_consumer_lag{group,topic}` (gauge)

---

## 4. 알림 규칙 (v2 MVP 필수)

### 4.1 거래 안전 (CRITICAL)

1) Upbit 차단(418)
- 조건(예시): 5분 내 418 1회 이상
- 조치: Kill Switch OFF + Upbit 호출 즉시 중단

2) UNKNOWN 발생
- 조건(예시): 5분 내 UNKNOWN 1회 이상
- 조치: 해당 market SUSPENDED + reconcile

3) THROTTLED(429) 폭증
- 조건(예시): 5분 내 THROTTLED 3회 이상
- 조치: 주문 빈도/동시성 축소 + 필요 시 Kill Switch OFF

### 4.2 가용성

4) 서버 Down (CRITICAL)
- 조건(예시): scrape 실패 2분 지속

5) 5xx 급증 (WARNING)
- 조건(예시): 5분 이동 윈도우에서 5xx rate 증가

### 4.3 리소스

6) 디스크 부족 (CRITICAL)
- 조건(예시): `/` 남은 용량 10% 미만 10분 지속

7) 메모리 부족 (WARNING)
- 조건(예시): 가용 메모리 10% 미만 10분 지속

---

## 5. 알림 전달 채널(권장)

v2 MVP 권장:
- Grafana Unified Alerting
- CRITICAL만 외부 채널로 발송(소음 방지)

선택지:
- Slack Incoming Webhook
- Email(SMTP)

---

## 6. 운영 규칙

- 알림은 “증상”이 아니라 “행동 트리거”여야 한다.
- 월 1회:
  - CRITICAL 알림 도착 테스트
  - 임계치 과민/둔감 조정
- 418/UNKNOWN 관련 알림은 비활성화하지 않는다.
