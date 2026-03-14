# 모듈러 모놀리식 + 헥사고날 + 이벤트 기반 백엔드 아키텍처

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

이 문서는 Spring Boot 백엔드의 **상위 설계 원칙**을 고정한다.  
구체적인 패키지/클래스 규칙은 `spring-boot-conventions.md`, 주문/이벤트 파이프라인은 `order-pipeline.md`, `event-bus.md`를 따른다.

---

## 1. 추천 방향(결론)

**배포는 하나**, **코드는 도메인별로 분리**, **모듈 내부는 헥사고날**, **모듈 간 통신은 API 또는 도메인 이벤트**, **운영 관측과 아키텍처 검증은 자동화**한다.

- **Modular Monolith**: 단일 배포 단위, 도메인 기준 모듈 분리  
- **Hexagonal inside modules**: 각 모듈 내부만 포트/어댑터 구조  
- **DDD Lite**: 애그리거트/값 객체 수준, 과한 DDD 금지  
- **Event-driven module coupling**: 모듈 간 주된 연동은 이벤트 발행/구독  
- **Spring Modulith**(선택 P1+): 구조 검증, 문서 생성, 모듈별 통합 테스트, 런타임 상호작용 관측

Martin Fowler는 마이크로서비스가 분산 호출·최종 일관성·운영 복잡성 비용이 크기 때문에 **많은 상황에서 모놀리식이 더 낫다**고 정리하며, 강한 모듈 경계는 모놀리식에서도 만들 수 있다고 설명한다.  
Spring Modulith는 “Spring Boot용 도메인 중심 모듈 애플리케이션을 만들기 위한 opinionated toolkit”으로, outbox 기반 externalization·테스트·observability를 지원한다.

> 실무 한 줄: **“Spring Boot 하나로 시작하되, 코드 구조는 도메인 모듈 + 헥사고날 + 이벤트로 짜고, (가능하면) Spring Modulith로 경계를 강제한다.”**

---

## 2. 왜 이 조합인가

### 2.1 유지보수성
`controller/service/repository`를 프로젝트 전체 공용 레이어로만 쪼개면, 기능 하나 수정 시 여러 패키지를 횡단하게 된다. **도메인 기준으로 모듈을 나누면 변경 영향 범위가 좁아진다.** Spring도 코드 구조를 도메인에 맞추는 쪽이 이해·유지보수 가능한 애플리케이션으로 이어진다고 설명한다.

### 2.2 운영 단순성
마이크로서비스는 서비스 간 네트워크 호출, 배포 파이프라인 다중화, 장애 전파, 분산 추적, 데이터 일관성 이슈가 필수로 따른다. 단일 애플리케이션은 이런 분산 비용이 없어 초기 생산성과 운영 안정성이 높다. Everbit v2는 1인 전용이며 API는 단일 VM(OCI E2.1.Micro), DB는 Supabase로 분리되어 있어 운영 단순성을 유지한다.

### 2.3 성능 기본값
같은 프로세스 안에서 모듈이 호출되므로 서비스 간 네트워크 홉이 없고, 분산 시스템 특유의 느린 원격 호출과 실패 가능성을 피할 수 있다. 튜닝 포인트는 **서비스 분리**보다 **DB 쿼리, 인덱스, 캐시, 락, 외부 API fan-out**에 둔다.

---

## 3. 설계 원칙

### 3.1 최상위는 도메인 모듈

**Everbit v2 기준** 도메인 모듈 예시는 아래와 같다. (커머스의 order/inventory/payment가 아님.)

```
com.everbit.everbit
├─ EverbitApplication.java
├─ auth          # 로그인/세션(OAuth2, JWT, Refresh)
├─ user          # 소유자·Upbit 키·Kill Switch
├─ trade         # Signal, OrderIntent, OrderAttempt, 실행, 정합성
├─ strategy      # 전략 설정(EXTREME_FLIP, STRUCTURE_LIFT, PRESSURE_SURGE 등)
├─ backtest      # 백테스트 job/결과
├─ notification  # Web Push 구독/발송
├─ dashboard     # 읽기 전용 요약/대시보드 API
└─ outbox        # EventBus 추상화, Outbox 발행/워커/디스패치
```

핵심: `trade/service`, `user/service`가 아니라 **`trade`**, **`user`** 자체가 하나의 업무 모듈이다.  
각 모듈 내부만 헥사고날로 나눈다.

### 3.2 모듈 내부 구조(헥사고날)

모듈마다 다음 레이어를 둔다.

| 레이어 | 역할 |
|--------|------|
| **api** | Controller, request/response DTO |
| **application** | 유스케이스, 트랜잭션, 오케스트레이션, port(in/out), 이벤트 발행 |
| **domain** | 엔티티, 애그리거트, 값 객체, 도메인 규칙 |
| **infrastructure** | JPA 구현체, 외부 API client, 메시지 브로커 adapter |

예시(`trade` 모듈):

```
trade/
├─ api/              # OrderController, PlaceOrderRequest 등
├─ application/
│  ├─ port/
│  │  ├─ in/         # PlaceOrderUseCase
│  │  └─ out/        # OrderRepository, EventBus
│  ├─ service/       # PlaceOrderService
│  └─ event/         # OrderPlaced 등 도메인 이벤트
├─ domain/           # Order, OrderIntent, OrderAttempt
└─ infrastructure/   # OrderRepositoryImpl, UpbitOrderAdapter(외부 호출은 integrations 쪽으로 위임)
```

**프로젝트 전체**에 Clean Architecture를 과하게 적용하지 않고, **“모듈 내부는 헥사고날, 프로젝트 전체는 모듈러 모놀리식”**으로 균형을 잡는다.

### 3.3 모듈 간 통신: API vs 이벤트

- **동기·직접 호출이 필요한 경우**: 다른 모듈의 **공개 API(포트 인)** 만 호출한다.  
  - 예: 주문 전 Kill Switch 확인 → `user` 모듈의 `KillSwitchQueryService.isAccountEnabled(ownerId)`  
- **부가/후속 처리**: **도메인 이벤트**를 발행하고, 관심 있는 모듈이 구독한다.  
  - 예: `OrderAccepted` 발행 → `notification` 모듈이 Web Push 발송  
- **금지**: 다른 모듈의 Repository/엔티티 직접 참조, 다른 모듈의 내부 구현에 의존.

v2 MVP에서는 이벤트 버스 구현체가 **PostgreSQL Outbox/Queue**이므로, “이벤트 발행” = 트랜잭션 내 `outbox_event` INSERT.  
SoT: `docs/architecture/event-bus.md`, `docs/adr/0009-postgres-outbox-queue-v2-mvp.md`

### 3.4 “중요 흐름은 동기, 부가 흐름은 비동기”

Everbit 기준으로 자른다.

- **동기(같은 요청/트랜잭션 경계 안)**  
  - 주문 의도 확정: Signal → 리스크 게이트 → OrderIntent 저장  
  - **Outbox 발행까지**: OrderIntent 저장 + `CreateOrderAttempt` 커맨드 INSERT (동일 트랜잭션)  
  - 사용자 응답은 “의도 접수됨” 수준에서 끝낸다.

- **비동기(워커/이벤트 구독)**  
  - Order Executor: `everbit.trade.command` 소비 → Upbit 주문 생성(Attempt 실행)  
  - 푸시 알림: `OrderAccepted` 수신 → Web Push 발송  
  - 백테스트 실행: `everbit.backtest.command` 소비 → 시뮬레이션  
  - 재고/결제는 없음(암호자산 단일 거래소 Upbit만 사용).

**결제처럼 “지금 성공/실패를 반드시 알아야 하는 것”은 동기로 둔다.** Everbit에서는 Upbit 주문 생성이 “동기 HTTP 응답”이 아니라 **Attempt 실행(워커)** 이므로, 이미 비동기 파이프라인이다. 이 경계는 `order-pipeline.md`가 정의한 대로 유지한다.

### 3.5 DB는 하나, 소유권은 모듈별

물리 DB는 하나(PostgreSQL)여도 된다. 원칙만 지킨다.

- **user**: `app_user`, `upbit_key`, `kill_switch` 등만 직접 접근  
- **trade**: `signal`, `order_intent`, `order_attempt`, `upbit_order`, `fill`, `position`, `market_state` 등만 직접 접근  
- **outbox**: `outbox_event` 발행/claim/상태 전이  
- **다른 모듈의 Repository를 주입해 쓰지 않음**  
- 조회가 필요하면 **공개 API(포트 인)** 또는 **이벤트 기반 read model**로 해결

테이블·제약 SoT: `docs/architecture/data-model.md`, `docs/db/schema-v2-mvp.sql`

---

## 4. Everbit 적용: 좋은 예 / 나쁜 예

### 4.1 나쁜 예

`OrderIntentService`가 모든 것을 직접 호출하는 구조:

```java
orderIntentRepository.save(intent);
eventBus.publish(...);
upbitClient.createOrder(...);        // 외부 호출을 트랜잭션 안에서
notificationService.sendPush(...);   // 타 모듈 직접 호출
dashboardService.invalidateCache(...);
```

문제: 주문 기능이 타 모듈 사정까지 알아야 하고, 테스트 시 mock이 늘어나며, 주문 수정이 결제/알림 코드 수정으로 이어지고, 나중에 경계를 나누기 어렵다.

### 4.2 좋은 예

주문 모듈은 **주문 의도 확정 + Outbox 발행**까지만 책임진다.

```java
// 동일 트랜잭션
orderIntentRepository.save(intent);
eventBus.publish(new CreateOrderAttempt(intent.getId(), attemptNo, ...));
return intent.getPublicId();
```

이후:
- **Order Executor 워커**: `CreateOrderAttempt` 소비 → Upbit 주문 생성 → `OrderAccepted` 발행  
- **notification 모듈**: `OrderAccepted` 구독 → Web Push 발송  
- **dashboard**: 조회 시 read model 또는 이벤트 기반 갱신

주문 모듈은 **주문 규칙**만 알면 되고, 나머지는 이벤트로 느슨하게 연결된다.  
이 흐름은 `docs/architecture/order-pipeline.md`, `docs/architecture/event-bus.md`에 맞춘다.

---

## 5. 운영·관측

아키텍처는 코드 구조만으로 끝나지 않고, 운영이 붙어야 한다.

- **Actuator + Observability**  
  Spring Boot는 logging, metrics, traces 3축으로 observability를 제공하며, Micrometer Observation 기반 자동 구성 지원.

- **Liveness / Readiness 분리**  
  Kubernetes 사용 시 Boot의 liveness/readiness health group 사용. Liveness는 외부 시스템에 의존하지 않도록, Readiness는 신중히 외부 의존성 포함.

- **모듈 관측(Spring Modulith 사용 시)**  
  모듈 구조를 actuator endpoint로 노출하고, 모듈 간 상호작용에 대한 metrics/traces 수집 가능.

- **문서 자동 생성(Spring Modulith)**  
  Documenter로 C4/UML 컴포넌트 다이어그램, Application Module Canvas 생성 가능.

---

## 6. 테스트 전략

구조에 맞춰 테스트를 둔다.

- **모듈 구조 검증(Spring Modulith 사용 시)**  
  `ApplicationModules.of(EverbitApplication.class).verify()` 로 모듈 간 cycle·의존 방향 검증.

- **모듈 단위 통합 테스트**  
  `@ApplicationModuleTest` 로 해당 모듈 중심 부트스트랩 후 통합 테스트.

- **P0 회귀( Everbit 강제)**  
  주문 멱등, 429/418 처리, UNKNOWN 수렴, Kill Switch, Outbox publish 동일 트랜잭션 등은 `docs/testing/strategy.md`, `docs/architecture/order-pipeline.md`에 따라 회귀 테스트 유지.

---

## 7. 성능·기타

- 단일 애플리케이션이므로 분산 트랜잭션, 서비스 간 RTT, 장애 전파 경로가 줄어든다. 성능 이슈 시 원인 추적이 상대적으로 쉽다.
- Java 21+에서 virtual threads 사용 시, Boot 문서의 pinned virtual threads·daemon 동작 주의사항을 따른다. **I/O bound 서비스에서 검증 후 적용**하는 옵션으로 둔다.

---

## 8. 피할 것

- 프로젝트 전체를 `controller / service / repository / entity` 패키지로만 나누는 구조  
- 모듈 간 Repository/엔티티 직접 참조  
- `common`, `util`, `shared`에 비즈니스 로직을 계속 넣는 구조  
- 처음부터 Kafka, Saga, 서비스 디스커버리, 분산 트레이싱 풀세트로 시작  
- “나중에 분리할 수 있게”라는 명분으로 지금부터 MSA 운영 비용을 전부 떠안는 구조  

---

## 9. 언제 마이크로서비스를 고려할까

아래 중 여러 개가 동시에 해당할 때 분리를 검토한다.

- 팀이 여러 스쿼드로 커져 **독립 배포**가 실제로 필요함  
- 특정 모듈만 트래픽/배포 주기가 압도적으로 다름  
- 데이터/보안/규제 경계가 강하게 갈림  
- 한 모듈 장애가 전체 배포 단위를 묶는 것이 치명적임  

그 전까지는 **모듈러 모놀리식이 비용·속도·안정성 측면에서 유리**하다.  
Everbit v2는 1인 전용·단일 VM이므로, 당분간 MSA 전환은 목표가 아니다.

---

## 10. Everbit 문서와의 관계

| 문서 | 역할 |
|------|------|
| **본 문서** | 상위 설계: 모듈러 모놀리식 + 헥사고날 + 이벤트, 모듈 목록·원칙 |
| [아키텍처 개요](./overview.md) | 시스템 컨텍스트, 배포 토폴로지, 런타임 컴포넌트 |
| [컴포넌트/모듈 경계](./components.md) | Everbit API/Domain/Adapter/EventBus 구체 매핑 |
| [Spring Boot 코드 규칙](./spring-boot-conventions.md) | 패키지/엔티티/DTO/서비스/테스트 규칙 |
| [주문 파이프라인](./order-pipeline.md) | 멱등/재시도/레이트리밋/UNKNOWN/Kill Switch (강제) |
| [Event Bus/Outbox](./event-bus.md) | outbox_event, 스트림, 워커, 재시도 |
| [데이터 모델](./data-model.md) | 테이블/유니크/상태 SoT |
| [ADR-0009](./../adr/0009-postgres-outbox-queue-v2-mvp.md) | Kafka 제외, PostgreSQL Outbox 채택 |
| [AGENTS.md](../../AGENTS.md) | Cursor/에이전트 준수 규칙(문서 우선, 보안, 주문 불변 조건) |

---

## 11. 참고 자료

- [Martin Fowler – Microservices](https://martinfowler.com/microservices/)
- [Martin Fowler – Microservice Trade-Offs](https://martinfowler.com/articles/microservice-trade-offs.html)
- [Martin Fowler – Integration Database (비권장 패턴)](https://martinfowler.com/bliki/IntegrationDatabase.html)
- [Spring Modulith – Fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html)
- [Spring Modulith – Application Events](https://docs.spring.io/spring-modulith/reference/events.html)
- [Spring Modulith – Documenting Application Modules](https://docs.spring.io/spring-modulith/reference/documentation.html)
- [Spring Modulith – Verifying Application Module Structure](https://docs.spring.io/spring-modulith/reference/verification.html)
- [Spring Modulith – Production-ready Features](https://docs.spring.io/spring-modulith/reference/production-ready.html)
- [Spring Boot – Observability](https://docs.spring.io/spring-boot/reference/actuator/observability.html)
- [Spring Boot – Endpoints (Health)](https://docs.spring.io/spring-boot/reference/actuator/endpoints.html)
- [Spring Boot – SpringApplication (Virtual threads 등)](https://docs.spring.io/spring-boot/reference/features/spring-application.html)
