# Backend TDD 실무 템플릿 (Spring Boot)

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-09 (Asia/Seoul)

이 문서는 **Spring Boot 백엔드에서 TDD를 실무적으로 적용하기 위한 패키지 구조·테스트 계층 규칙·예시**를 고정한다.  
TDD 루프/적용 범위/P0 우선순위는 `docs/testing/tdd.md`가 SoT이다.

**한 줄 요약:**  
비즈니스는 plain JUnit으로 빨리 검증하고, 기술 경계는 slice test로 자르고, 전체 흐름은 몇 개만 `@SpringBootTest`로 확인하는 구조.

**테스트 계층(순서 고정):**

1. 서비스/도메인 단위 테스트
2. 컨트롤러 슬라이스 테스트
3. 리포지토리 슬라이스 테스트
4. 소수의 전체 통합 테스트

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

**예시 1: InMemory fake 사용**

```java
// 예: 주문 유스케이스 또는 회원가입 유스케이스
class PlaceOrderServiceTest {

    private final OrderRepository orderRepository = new InMemoryOrderRepository();
    private final UuidGenerator uuidGenerator = () -> java.util.UUID.fromString("018e1234-5678-7000-8000-000000000000");
    private final PlaceOrderUseCase useCase =
            new PlaceOrderService(orderRepository, uuidGenerator);

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

**예시 2: Mockito 사용(경계 검증)**

```java
@ExtendWith(MockitoExtension.class)
class RegisterUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RegisterUserService registerUserService;

    @Test
    void register_throws_exception_when_email_already_exists() {
        // given
        String email = "test@everbit.com";
        given(userRepository.existsByEmail(email)).willReturn(true);

        // when / then
        assertThatThrownBy(() -> registerUserService.register(email, "홍길동"))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("이미 가입된 이메일입니다.");

        then(userRepository).should().existsByEmail(email);
        then(userRepository).should(never()).save(any());
    }
}
```

핵심: `verify(repository).save(...)` 같은 **상호작용 검증이 아니라**, 저장된 **상태(결과)**를 검증한다.  
이 테스트가 먼저 있어야 하며, **회원가입/주문의 핵심 규칙은 Spring 없이 검증 가능해야 한다.**  
여기서는 HTTP, JSON, SecurityFilter, DB는 신경 쓰지 않는다.

---

## 5. Controller slice 예시(@WebMvcTest + MockMvc)

입력 검증, 상태 코드, JSON 계약만 확인한다. use case는 `@MockBean`으로 대체.

- **MockMvc**: 현재 프로젝트는 Spring MVC 기준이므로 `@WebMvcTest`가 MockMvc를 자동 구성.
- **보안 설정**: `@Import(SecurityConfig.class)`로 명시적으로 가져온다. 보안 설정이 별도 클래스라면 필수.
- **CSRF**: POST/PUT/DELETE 시 `with(csrf())` 포함(CSRF가 켜져 있을 때).
- **인증 테스트**: `@WithMockUser(username = "admin", roles = "ADMIN")` 사용. `spring-security-test` 의존성 필요.
- **addFilters = false**: JSON 스펙·validation·status code만 볼 때 보안 필터를 끈다. 보안 테스트와 API 스펙 테스트를 분리.

**예시 1: 인가 규칙만 검증(보안 필터 포함)**

```java
@WebMvcTest(DashboardController.class)
@Import(SecurityConfig.class)
class DashboardControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    DashboardService dashboardService;

    @Test
    void 인증없으면_401() throws Exception {
        mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void 인증있으면_200() throws Exception {
        given(dashboardService.getSummary(any())).willReturn(new DashboardSummary(...));

        mockMvc.perform(get("/api/v2/dashboard/summary").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
```

**예시 2: API 스펙만 검증(보안 필터 제외)**

```java
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    RegisterUserUseCase registerUserUseCase;

    @Test
    void create_user_returns_201() throws Exception {
        given(registerUserUseCase.register(any()))
                .willReturn(new RegisterUserResult("user-1"));

        mockMvc.perform(post("/api/v2/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""" {"email":"test@everbit.com","name":"홍길동"} """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"));
    }
}
```

검증 대상: 요청/응답 스펙, validation, 상태 코드, 인증/인가 결과, 예외가 어떤 HTTP 응답으로 번역되는지.

---

## 6. Persistence slice 예시(@DataJpaTest + Testcontainers)

JPA 매핑, unique 제약, 쿼리만 검증한다. Adapter를 쓸 경우 `@Import(Adapter.class)`로 포함.

- **Postgres 사용**: H2 대체 금지. SQL, JSONB, SKIP LOCKED, 인덱스 동작이 다름.
- **설정**: `@ActiveProfiles("test")`로 `application-test.yml` 로드. DB 연결은 Testcontainers가 `@DynamicPropertySource` 또는 `@ServiceConnection`으로 주입.
- **@DynamicPropertySource**: 테스트 컨텍스트에 동적 속성 등록 시 공식 방식.

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ActiveProfiles("test")
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void exists_by_email_returns_true_when_user_exists() {
        userRepository.save(User.create("test@everbit.com", "홍길동"));

        boolean result = userRepository.existsByEmail("test@everbit.com");

        assertThat(result).isTrue();
    }
}
```

검증 대상: Querydsl 커스텀 조회, 정렬/페이징, unique 제약/인덱스 기대 동작, soft delete 조건, 시간대/UTC 저장.

---

## 7. Flow 테스트 예시(@SpringBootTest, 최소만)

전체 컨텍스트 + MockMvc로 핵심 happy path 1~3개만 유지한다.

- **프로필**: `@ActiveProfiles("test")`로 `application-test.yml` 로드.
- **@ServiceConnection**: Spring Boot 3.1+ service connection이 connection-related property보다 우선. `spring-boot-testcontainers` 있으면 사용 가능.
- **설정값**: raw string 대신 `application-test.yml`의 값을 참조한다.

```java
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestSupport {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");
}

class OrderFlowTest extends IntegrationTestSupport {

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

권장 대상: 로그인 성공/실패, JWT 재발급, 권한 없는 요청 401/403, DB 마이그레이션 이후 핵심 플로우 1개, 카카오 OAuth 콜백 핵심 플로우 1개.

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

## 10. Config 테스트(ApplicationContextRunner)

Config 테스트는 **빈 wiring**이 아니라 **속성 바인딩·부팅 실패**만 검증한다.

- `@Value` 대신 `@ConfigurationProperties` 사용 권장.
- `ApplicationContextRunner`로 property 주입 후 context 성공/실패를 검증.

```java
class AuthPropertiesTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withUserConfiguration(EnableAuthPropertiesConfig.class)
                    .withPropertyValues(
                            "auth.jwt-access-secret=test-access-secret-min-32-chars-required",
                            "auth.jwt-refresh-secret=test-refresh-secret-min-32-chars-required",
                            "auth.jwt-access-ttl-seconds=900",
                            "auth.jwt-refresh-ttl-seconds=1209600",
                            "auth.allowed-origins=http://localhost:3000"
                    );

    @Test
    void auth_properties_정상_바인딩() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            AuthProperties props = context.getBean(AuthProperties.class);
            assertThat(props.jwtAccessTtlSeconds()).isEqualTo(900);
        });
    }

    @Test
    void 필수값_없으면_부팅_실패() {
        new ApplicationContextRunner()
                .withUserConfiguration(EnableAuthPropertiesConfig.class)
                .withPropertyValues("auth.jwt-access-ttl-seconds=900")
                .run(context -> assertThat(context).hasFailed());
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(AuthProperties.class)
    static class EnableAuthPropertiesConfig {}
}
```

검증 대상: 필요한 속성 바인딩, 필수값 누락 시 실패, 프로필/테스트 속성 오버라이드.

## 11. 테스트 설정(application-test.yml)

테스트 전용 설정은 `application-test.yml`에 두고, raw string 대신 이 값을 참조한다.

- **DB**: 운영과 격리. Testcontainers Postgres(ephemeral) 사용. `@ActiveProfiles("test")`로 로드.
- **JPA**: `ddl-auto: create-drop` 등 테스트용 값. validate는 Flyway 없이 빈 DB에서는 막힌다.
- **인증/외부 연동**: 테스트용 시크릿·URL. `auth.*` 등 `@ConfigurationProperties`로 주입.
- **로깅**: 테스트 시 불필요한 로그 억제.

**정적 vs 동적 속성:**

- **정적**: `@TestPropertySource(properties = { "auth.jwt-access-secret=...", ... })` — 인라인 값은 높은 우선순위.
- **동적**: `@DynamicPropertySource` — Testcontainers 포트처럼 런타임에 결정되는 값.

## 12. 의존성

- `spring-boot-starter-test`: JUnit Jupiter, AssertJ, Hamcrest, Mockito 포함.
- `spring-security-test`: `@WithMockUser`, `csrf()` 등 보안 테스트용. **별도 추가 필요.**

## 13. 참고

- TDD 루프/범위/P0: `docs/testing/tdd.md`
- 테스트 전략/CI 게이트: `docs/testing/strategy.md`
- Spring Boot 코드/패키지 규칙: `docs/architecture/spring-boot-conventions.md`
- Spring Boot 3.x 기준 slice test: [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html)  
- Testcontainers `@ServiceConnection`: Spring Boot 3.1+ 사용 시 datasource 자동 주입 가능.
