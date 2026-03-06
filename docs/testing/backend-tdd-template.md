# Backend TDD 실무 템플릿 (Spring Boot)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

이 문서는 **Spring Boot 백엔드에서 TDD를 실무적으로 적용하기 위한 패키지 구조·테스트 계층 규칙·예시**를 고정한다.  
TDD 루프/적용 범위/P0 우선순위는 `docs/testing/tdd.md`가 SoT이다.

**한 줄 요약:**  
비즈니스는 plain JUnit으로 빨리 검증하고, 기술 경계는 slice test로 자르고, 전체 흐름은 몇 개만 `@SpringBootTest`로 확인하는 구조.

---

## 1. 핵심 규칙(고정)

- **TDD 시작점은 항상 use case 테스트**이다.
- **비즈니스 규칙 테스트에는 Spring 컨텍스트를 띄우지 않는다.**
- **Mock은 controller slice, external client slice 정도에서만 제한적으로 쓴다.**
- **JPA/쿼리 검증은 별도 slice로 분리한다.**
- **full context 테스트는 핵심 흐름 몇 개만 둔다.**
- **테스트 패키지 구조는 운영 코드와 1:1로 맞춘다.**

---

## 2. 프로젝트/테스트 구조

Spring Boot 권장처럼 루트 패키지 아래 **도메인 기준 + 모듈 내부 계층 분리**를 따른다.  
everbit의 실제 패키지명은 `docs/architecture/spring-boot-conventions.md`의 디렉터리 구조를 따른다. 아래는 **테스트 배치 원칙**을 위한 논리 구조 예시이다.

```text
src/main/java/com.everbit.everbit/
  global/...
  user/          (또는 trade, outbox 등)
    entity/
    repository/
    service/     ← UseCase/애플리케이션 계층
    dto/
    api/         ← Controller (필요 시)

src/test/java/com.everbit.everbit/
  user/
    service/           ← *ServiceTest (plain JUnit, Spring 없음)
    api/               ← *ControllerTest (@WebMvcTest)
    infrastructure/
      persistence/     ← *RepositoryAdapterTest (@DataJpaTest)
    flow/              ← *FlowTest (@SpringBootTest, 최소만)
  support/
    IntegrationTestSupport.java   ← @ActiveProfiles("test") 등 공통
```

규칙:
- `service`(유스케이스) 테스트는 `src/test` 아래 동일 패키지에 두고, **Spring 없이** 실행한다.
- Controller 테스트는 `@WebMvcTest(해당Controller.class)` 한정.
- JPA adapter 테스트는 `@DataJpaTest` + Testcontainers(Postgres).
- 전체 흐름은 `flow` 패키지에 1~3개 수준만 둔다.

---

## 3. 테스트 계층별 규칙

| 계층 | 도구 | Spring | Mock | 비고 |
|------|------|--------|------|------|
| application/domain(유스케이스) | JUnit5, AssertJ | 없음 | fake/stub 우선 | 상태 기반 검증 |
| api(Controller) | @WebMvcTest | slice만 | @MockitoBean(use case) | 입력/응답/계약만 |
| persistence | @DataJpaTest | slice만 | 없음 | JPA 매핑, 제약, 쿼리 |
| external REST client | @RestClientTest | slice만 | MockRestServiceServer | URL/헤더/body/응답 매핑 |
| flow | @SpringBootTest | full | 필요 시만 | happy path 최소 |

- **application/domain**: DB 없음, `InMemory*Repository` 또는 stub으로 충분히 검증.
- **api**: use case는 `@MockitoBean`으로 대체해도 됨(이 레벨의 관심사는 컨트롤러 입출력이기 때문).
- **persistence**: production과 동일한 DB 종류(Postgres)를 Testcontainers로 사용. H2 대체 금지(JSONB, SKIP LOCKED 등 동작 차이).
- **flow**: `@SpringBootTest` + `@AutoConfigureMockMvc` 또는 `webEnvironment = RANDOM_PORT`. 테스트 전용 프로필은 `@ActiveProfiles("test")`로 상위 클래스에 고정.

---

## 4. UseCase 테스트 예시(상태 기반, Spring 없음)

비즈니스 규칙을 **가장 먼저** 검증한다. Controller가 아니다.

```java
// 예: 주문 유스케이스 또는 회원가입 유스케이스
class PlaceOrderServiceTest {

    private final OrderRepository orderRepository = new InMemoryOrderRepository();
    private final UlidGenerator ulidGenerator = () -> "01J fixed";
    private final PlaceOrderUseCase useCase =
            new PlaceOrderService(orderRepository, ulidGenerator);

    @Test
    void 정상_조건이면_주문이_저장된다() {
        Long id = useCase.handle(new PlaceOrderCommand(...));

        Order saved = orderRepository.findById(id).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void 킬스위치_꺼져_있으면_주문을_수행하지_않는다() {
        // given: accountEnabled = false
        assertThatThrownBy(() -> useCase.handle(...))
                .isInstanceOf(AccountDisabledException.class);
    }

    static class InMemoryOrderRepository implements OrderRepository {
        private final Map<Long, Order> store = new HashMap<>();
        // ...
    }
}
```

핵심: `verify(repository).save(...)` 같은 **상호작용 검증이 아니라**, 저장된 **상태(결과)**를 검증한다.  
이 테스트가 먼저 있어야 하며, **회원가입/주문의 핵심 규칙은 Spring 없이 검증 가능해야 한다.**

---

## 5. Controller slice 예시(@WebMvcTest)

입력 검증, 상태 코드, JSON 계약만 확인한다. use case는 `@MockitoBean`으로 대체.

```java
@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PlaceOrderUseCase placeOrderUseCase;

    @Test
    void 주문_요청을_받으면_201을_반환한다() throws Exception {
        given(placeOrderUseCase.handle(any())).willReturn(1L);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" {"market":"KRW-BTC", ...} """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }

    @Test
    void 마켓_형식이_잘못되면_400을_반환한다() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" {"market":"invalid"} """))
                .andExpect(status().isBadRequest());
    }
}
```

---

## 6. Persistence slice 예시(@DataJpaTest + Testcontainers)

JPA 매핑, unique 제약, 쿼리만 검증한다. Adapter를 쓸 경우 `@Import(Adapter.class)`로 포함.

```java
@Testcontainers
@DataJpaTest
@Import(OrderRepositoryAdapter.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OrderRepositoryAdapterTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    OrderRepository orderRepository;

    @Test
    void 주문을_저장하고_식별자로_다시_조회할_수_있다() {
        Order saved = orderRepository.save(Order.create(...));
        assertThat(orderRepository.findById(saved.getId())).isPresent();
    }
}
```

---

## 7. Flow 테스트 예시(@SpringBootTest, 최소만)

전체 컨텍스트 + MockMvc로 핵심 happy path 1~3개만 유지한다.

```java
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class OrderFlowTest extends IntegrationTestSupport {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    MockMvc mockMvc;

    @Test
    void 주문_전체_흐름이_동작한다() throws Exception {
        mockMvc.perform(post("/api/orders")...)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists());
    }
}
```

---

## 8. 실제 개발 순서(권장)

1. Use case 테스트 작성(plain JUnit, InMemory fake)
2. 최소 구현으로 GREEN
3. 도메인 규칙(중복/정책) 추가
4. Controller 테스트(`@WebMvcTest`) 작성
5. Persistence 테스트(`@DataJpaTest`) 작성
6. 외부 API 있으면 `@RestClientTest` 작성
7. 마지막에 flow 테스트 1~3개 추가

**use case → controller slice → persistence slice → external client slice → minimal flow**

---

## 9. 꼭 지킬 것(템플릿 기준)

- private 메서드 테스트하지 않는다.
- service 테스트에서 Spring 띄우지 않는다.
- service 테스트에서 mock 남발하지 않는다.
- controller 테스트에서 비즈니스 로직 검증하지 않는다.
- repository 테스트에서 API status code 보지 않는다.
- full context 테스트를 많이 만들지 않는다.

---

## 10. 참고

- TDD 루프/범위/P0: `docs/testing/tdd.md`
- 테스트 전략/CI 게이트: `docs/testing/strategy.md`
- Spring Boot 코드/패키지 규칙: `docs/architecture/spring-boot-conventions.md`
- Spring Boot 3.5.x 기준 slice test: [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)  
- Testcontainers `@ServiceConnection`: Spring Boot 3.1+ 사용 시 datasource 자동 주입 가능.
