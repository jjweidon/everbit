# TDD 개발 규칙 (Backend: Spring Boot)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

everbit v2 백엔드는 **TDD(Test-Driven Development)**를 기본 개발 방식으로 채택한다.

원칙: **실패(RED) → 성공(GREEN) → 리팩토링(REFACTOR)**  
목표는 “테스트 커버리지”가 아니라, **실거래에서 사고를 내는 영역(멱등/상태머신/레이트리밋/외부 연동)을 회귀 테스트로 고정**하는 것이다.

이 문서는 “테스트 작성 방식”이 아니라, 구현자가 흔들리지 않도록 **개발 루프/규칙/DoD**를 고정한다.

관련 SoT:
- 테스트 전략: `docs/testing/strategy.md`
- 테스트 매트릭스: `docs/testing/test-matrix.md`
- 주문 파이프라인(핵심 규칙): `docs/architecture/order-pipeline.md`
- Outbox/Queue: `docs/architecture/event-bus.md`
- 코드 컨벤션: `docs/architecture/spring-boot-conventions.md`

---

## 1. 적용 범위

TDD는 아래 범위에서 **강제**한다.

- 도메인 규칙/상태머신(주문/재시도/멱등/킬스위치)
- Outbox/Queue worker(클레임/재시도/스테일락 복구)
- Upbit REST/WS 연동(레이트리밋/418/timeout/UNKNOWN 수렴)
- 보안/정합성(키 암호화 저장, 유니크 제약 기반 멱등)

예외(허용):
- 단순 Wiring(Controller 라우팅/DI)이나 UI 표시용 DTO 조립처럼 “규칙이 거의 없는” 부분은 TDD를 완화할 수 있다.  
  단, P0 기능의 경우 최종적으로 Integration/E2E 테스트가 반드시 존재해야 한다.

---

## 2. RED → GREEN → REFACTOR 루프(강제)

### 2.1 RED(실패)
- **테스트부터 작성**한다.
- 테스트는 “지금 구현이 없어서 실패”하거나 “버그를 재현해서 실패”해야 한다.
- 실패 이유는 1개여야 한다(한 테스트에서 여러 규칙을 동시에 검증하지 않는다).

### 2.2 GREEN(성공)
- 테스트를 통과시키는 **최소 코드**만 작성한다.
- 과도한 일반화/추상화는 금지한다.
- 외부 연동/DB/시간이 엮이면:
  - “단위(Unit)로 충분한가?”를 먼저 판단하고
  - 단위로 불가능하면 **통합(Integration) 테스트를 먼저 RED로 만든다**.

### 2.3 REFACTOR(리팩토링)
- 테스트가 GREEN인 상태를 유지하면서
  - 중복 제거
  - 네이밍 정리
  - 책임 분리(서비스/핸들러/레포 분리)
  - 상태 전이 가드(불법 전이 방지)
  를 수행한다.
- 리팩토링 단계에서만 구조를 개선한다(RED/GREEN 단계에서 구조개선 금지).

---

## 3. 테스트 우선순위(P0)

everbit에서 “테스트가 없으면 곧 사고로 이어지는” 영역을 P0로 고정한다.

### 3.1 주문/멱등/상태머신(P0)
반드시 회귀 테스트로 고정해야 하는 시나리오:
- Signal 멱등: 동일 캔들/전략/마켓 조합 재계산 시 중복 insert가 실패하거나 무시되는지
- OrderIntent 멱등: 동일 signal_id + intent_type 중복 생성 방지
- OrderAttempt 멱등/순번: attempt_no 증가, identifier 재사용 금지(UNIQUE)
- UNKNOWN 수렴: timeout/5xx 시 재주문 금지 + reconcile로 회복
- KillSwitch 강제: account_enabled=false이면 executor가 주문을 수행하지 않음

### 3.2 레이트리밋/418(P0)
- 429(THROTTLED): **새 Attempt(attempt_no+1, 새 identifier)**로 재시도 트리거
- 418: 즉시 SUSPENDED 또는 정책에 따른 강제 중단 + 알림

### 3.3 Outbox/Queue(P0)
- PENDING → PROCESSING → DONE 전이
- 실패 → attempt_count 증가 → next_retry_at 백오프
- 워커 크래시/중단 후 재기동 시 **스테일 락 복구**로 이벤트가 다시 처리되는지

---

## 4. 레이어별 TDD 적용 가이드

### 4.1 Unit(순수 도메인)
대상:
- 상태머신, 정책 결정(주문 차단/허용), 파라미터 파생 규칙

규칙:
- Spring Context 없이 JUnit5 + AssertJ로 작성한다.
- 시간은 `Clock`을 주입해서 고정한다.
- ULID/랜덤 값은 테스트에서 결정적으로 만든다(섹션 6 참고).

### 4.2 Repository / DB 제약
대상:
- 유니크 제약(멱등), 복합키/공유PK 매핑, 인덱스 기반 조회

규칙:
- `@DataJpaTest` + Testcontainers(Postgres)를 기본으로 한다.
- H2로 대체하지 않는다(특히 SKIP LOCKED/JSONB/제약 동작이 달라진다).

### 4.3 UseCase(서비스 계층)
대상:
- “요청 → 트랜잭션 → Outbox 발행”의 정합성

규칙:
- `@SpringBootTest` + Testcontainers(Postgres)로 통합 테스트를 둔다.
- 도메인 규칙은 Unit에서 이미 검증되어야 하며, 서비스 통합 테스트는 “경계/트랜잭션/락/이벤트 발행”을 검증한다.

### 4.4 외부 연동(Upbit/Kakao/WebPush)
대상:
- REST 요청/응답/에러 매핑, 레이트리밋 처리, 재시도 정책

규칙:
- 개발/CI에서는 WireMock으로 컨트랙트를 고정한다.
- 실 Upbit 호출은 운영 검증 단계에서 “제한된 스모크”로만 수행한다.

### 4.5 비동기(Outbox Worker)
규칙:
- `Awaitility`를 사용해 “조건이 만족될 때까지 기다리는 방식”으로 작성한다.
- `Thread.sleep` 금지(테스트가 flaky해진다).

---

## 5. 테스트 네이밍/구조 규칙(강제)

- 패키지 구조는 `src/main/java`와 동일하게 미러링한다.
- 클래스명: `*Test`로 끝난다.
- 메서드명은 “행동”을 나타낸다.
  - 예: `shouldRejectOrderWhenKillSwitchDisabled()`
- 테스트 구조는 Given/When/Then(또는 Arrange/Act/Assert)로 통일한다.
- 한 테스트는 한 규칙만 검증한다.

권장 구조:
- `@Nested` + `@DisplayName`으로 시나리오를 묶는다.
- 반복되는 픽스처는 `Fixture` 클래스로 분리한다.

---

## 6. 결정적 테스트(Determinism) 표준

실거래 시스템 테스트에서 “우연히 통과”는 금지다.

### 6.1 시간 고정
- `Clock`을 Bean으로 제공하고, 테스트에서는 `Clock.fixed(...)`로 주입한다.

### 6.2 ULID/식별자 고정
- 프로덕션에서 ULID를 쓰더라도, 테스트에서는 결정적으로 생성할 수 있어야 한다.
- 권장:
  - `UlidGenerator` 인터페이스를 두고,
  - 테스트에서는 `FixedUlidGenerator("01J...")`로 고정한다.

```java
public interface UlidGenerator {
  String next();
}

public final class ThreadLocalUlidGenerator implements UlidGenerator {
  private static final ThreadLocal<de.huxhorn.sulky.ulid.ULID> TL =
      ThreadLocal.withInitial(de.huxhorn.sulky.ulid.ULID::new);
  @Override public String next() { return TL.get().nextULID(); }
}
```

---

## 7. 금지 사항(실전에서 사고나는 패턴)

- 테스트 없이 구현부터 시작(=TDD 위반)
- flaky 테스트 방치
- `Thread.sleep` 사용
- “모든 것을 Mockito로” 처리해서 DB 제약/락/트랜잭션을 검증하지 않는 테스트
- Spring Context를 남발해서 Unit 테스트까지 느리게 만드는 방식
- 임의 랜덤 데이터로 테스트(재현 불가)

---

## 8. PR / DoD(완료 기준) 규칙

- 모든 기능 PR은 최소 1개 이상의 테스트를 포함한다.
- 버그 수정은 **재현 테스트(RED) → 수정(GREEN)** 순서로 진행한다.
- 머지 조건:
  - Unit + Integration(Testcontainers) 전체 GREEN
  - 주문/Outbox P0 시나리오 회귀 테스트 GREEN
- 리팩토링 PR도 테스트가 GREEN인 상태를 유지한다.

---

## 9. 예시: Kill Switch가 주문을 차단해야 한다(RED → GREEN)

### 9.1 RED(테스트 먼저)
- Given: account_enabled=false
- When: OrderExecutor가 OrderAttempt를 실행
- Then: Upbit 호출이 일어나지 않고 attempt는 SUSPENDED(또는 CANCELED)로 수렴

> 실제 코드는 `docs/testing/test-matrix.md`의 케이스로 관리한다. 이 섹션은 “루프” 예시를 위한 설명이다.
