# Spring Boot 코드/클래스 규칙

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-03-06 (Asia/Seoul)

이 문서는 everbit v2 백엔드(Spring Boot)에서 **클래스/패키지/레이어/엔티티/DTO**를 어떤 규칙으로 작성할지 고정한다.

- **상위 아키텍처**(모듈러 모놀리식, 도메인 모듈, 헥사고날·이벤트): `docs/architecture/modular-monolith.md`
- DB 스키마/제약의 SoT: `docs/architecture/data-model.md`, `docs/db/schema-v2-mvp.sql`. 런타임 스키마는 Hibernate ddl-auto(`update`)로 생성·갱신(ADR-0003).
- JPA 복합키/공유PK 매핑 SoT: `docs/architecture/jpa-mapping.md`

---

## 1. 디렉터리/패키지 구조(강제)

> 목적: 파일이 늘어나도 “어디에 무엇이 있어야 하는지”가 흔들리지 않게 한다.

권장 루트:

```
com.everbit.everbit
  global/
    config/            # ConfigurationProperties, Jackson/JPA/보안 설정
    crypto/            # 암복호화(AES-GCM), Key management
    error/             # ErrorCode, 예외 계층, ExceptionHandler
    jpa/               # Auditing, converters, base entities
    util/              # UUID v7, 시간, 공용 유틸

  user/
    entity/
    repository/
    service/
    dto/
    mapper/

  trade/
    entity/
    repository/
    service/
    dto/
    mapper/

  outbox/
    entity/
    repository/
    service/           # publish(INSERT)
    worker/            # poll/claim/dispatch
    handler/           # 이벤트 핸들러

  integrations/
    upbit/
    kakao/
    webpush/
```

---

## 1.5 Lombok 사용 규칙(가독성)

가독성을 위해 **생성자·게터·세터·빌더** 등 Lombok으로 동일 표현이 가능한 보일러플레이트는 Lombok으로 간소화한다.

### 허용
| 어노테이션 | 용도 |
|-----------|------|
| `@Getter` | 엔티티, BaseEntity 등 읽기 전용 필드 |
| `@NoArgsConstructor(access = AccessLevel.PROTECTED)` | JPA 엔티티(프록시용) |
| `@AllArgsConstructor(access = AccessLevel.PRIVATE)` | 빌더와 함께 사용 시 |
| `@Builder(access = AccessLevel.PRIVATE)` | private 생성자 + static factory 패턴 |
| `@RequiredArgsConstructor` | `@Configuration`, 서비스 등 final 필드 주입 |
| `@Value` | 불변 설정/값 객체(필드 final, 생성자, equals/hashCode) |
| `@UtilityClass` | 유틸리티 클래스(private 생성자 + final class) |

### 금지
| 어노테이션 | 사유 |
|-----------|------|
| `@Data` | toString/equals/hashCode가 민감정보·지연로딩을 터뜨림 |
| `@Setter` (엔티티) | 상태 변경은 의도가 드러나는 메서드로만 |

### Record vs Lombok
- **Request/Response DTO**: Java record 우선(생성자·게터·equals/hashCode 자동).
- **엔티티**: record 불가(JPA 제약). Lombok `@Getter` + `@NoArgsConstructor` + `@Builder` 조합.

---

규칙:
- `controller`는 API 레이어에만 존재한다.
- `integrations/*`는 외부 프로토콜/에러/레이트리밋을 내부로 누수시키지 않는다.
- 엔티티는 절대 컨트롤러/DTO를 참조하지 않는다.

---

## 2. 네이밍 규칙(강제)

- Entity: `AppUser`, `OrderIntent`, `OutboxEvent` (단수)
- Repository: `AppUserRepository`
- Service:
  - 명령/상태변경: `*CommandService` 또는 `*Service`
  - 조회: `*QueryService` (규모가 커질 때 분리)
- DTO:
  - 요청: `*Request`
  - 응답: `*Response`
- Mapper:
  - 수작업이면 `*Mapper`
  - MapStruct면 `*Mapper` + `@Mapper`

---

## 3. 엔티티(Entity) 작성 규칙

### 3.0 테이블/컬럼 네이밍(Spring Boot 자동 매핑)

Spring Boot 기본 `SpringPhysicalNamingStrategy`가 **camelCase → snake_case**를 자동 적용한다.

- 엔티티 `AppUser` → 테이블 `app_user`
- 필드 `publicId` → 컬럼 `public_id`, `createdAt` → `created_at`
- 따라서 `@Table(name = "...")`, `@Column(name = "...")`, `@JoinColumn(name = "...")`를 **명시하지 않는다**.
- 예외: `@UniqueConstraint(columnNames = {...})`, 복합 `@JoinColumns` 등 구조적으로 필요할 때만 컬럼명 명시.

SoT: `docs/architecture/jpa-mapping.md` §1

### 3.1 엔티티에 대한 기본 정책

- 엔티티는 **Persistence 모델**이다. API 응답에 엔티티를 그대로 노출하지 않는다.
- `@Setter` 금지. 상태 변경은 **의도 이름이 드러나는 메서드**로만 허용한다.
  - 예: `changeEmail(...)`, `enableAccount()`, `disableAccount()`
- 연관관계 기본값:
  - `@ManyToOne(fetch = LAZY)`
  - `@OneToMany`는 가급적 엔티티에 컬렉션을 두지 않는다(필요 시만)
  - `@OneToOne`은 **양방향을 기본 금지**(필요 시만). 특히 민감정보/설정 테이블은 부모에서 접근 필드를 두지 않는 편이 안전하다.
- `@Data` 금지(실수로 toString/equals/hashCode가 민감정보/지연로딩을 터뜨린다).
- 로깅/직렬화 안전:
  - 엔티티는 원칙적으로 Jackson 직렬화 대상이 아니다.
  - 부득이하면 `@JsonIgnore` / `@ToString.Exclude`를 강제한다.

### 3.2 키 정책(엔티티에 반영)

- 내부 PK: `bigint identity` → `Long id` + `@GeneratedValue(IDENTITY)`
- 외부 노출/추적용: `public_id uuid`(UUID v7) → `UUID publicId`
  - API/URL/로그에는 `publicId.toString()`을 사용한다.
  - `id`는 외부에 노출하지 않는다.

UUID v7 생성 표준:
- 생성 책임은 **서비스/팩토리 레이어**가 가진다.
- `@PrePersist`로 fallback 설정하지 않는다. 엔티티는 ID 생성 로직을 알지 않는다.
- UUID v7 생성기는 thread-safe하게 제공한다(예: `com.github.f4b6a3.uuid.UuidCreator`).

```java
public final class Uuids {
  private Uuids() {}
  public static UUID next() {
    return UuidCreator.getTimeOrderedEpoch();
  }
}
```

### 3.3 시간 컬럼 매핑 규칙

- DB는 `timestamptz(UTC)`다.
- Java 타입은 `Instant`를 표준으로 한다(표시 시 KST 변환).
- v2 MVP 엔티티는 원칙적으로 **`BaseEntity(createdAt, updatedAt)` 하나로 통일**한다.
- 비즈니스 시각(`rotatedAt`, `suspendedAt`, `capturedAt`, `deliveredAt` 등)은 BaseEntity와 별도 컬럼으로 둔다.

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
```

구현 메모:
- append-only row라도 `updated_at`은 유지한다(일반적으로 insert 직후 `created_at == updated_at`).
- 팀 내에서 `LocalDateTime`을 쓰더라도 애플리케이션/JDBC/Hibernate timezone을 **UTC로 고정**해야 한다.

> 전제: `@EnableJpaAuditing` 활성화.

### 3.4 equals/hashCode/toString 규칙

- `equals/hashCode`는 기본적으로 **정의하지 않는 것**을 권장한다(프록시/영속성 컨텍스트 이슈).
- 반드시 필요하면 `id` 기반으로만 구현한다.
  - 단, `id == null`(영속화 전)인 상태에서는 동등성 비교를 기대하지 않는다.
- `toString`은 엔티티에 자동 생성하지 않는다.
  - 필요하면 민감/연관 필드는 무조건 제외한다.

### 3.5 Builder 사용 규칙(엔티티)

- 엔티티 클래스에 무제한 `@Builder`를 걸지 않는다.
  - `id`, `publicId`, 연관관계까지 builder에 섞이면 실수가 잦다.
- 권장: **private/package-private 생성자에 @Builder** + public static factory

---

## 4. 권장 엔티티 구조(기존 User 엔티티 기준 개선안)

### 4.1 기존 코드에서 반드시 바꿔야 하는 부분

기존 `User` 엔티티에서 아래는 v2 기준으로 금지한다.

1) **PK를 UUID String으로 직접 보유**
- v2는 내부 PK를 `Long(identity)`로 고정했다.
- UUID v7은 `publicId`로 별도 컬럼(uuid 타입)으로 둔다.

2) **Upbit Access/Secret을 평문 컬럼으로 저장**
- 키는 반드시 `upbit_key` 테이블에 **암호문(bytea)** 으로 저장한다.
- 엔티티도 분리한다(`AppUser` ↔ `UpbitKey`).

3) “봇 활성화” 같은 실행 플래그를 `User`에 직접 들고 있지 않음
- v2는 `kill_switch`가 실행 제어의 SoT다.

### 4.2 AppUser 엔티티(권장 템플릿)

> v2 MVP 스키마(`app_user`) 기준. `nickname/image/role` 같은 프로필 컬럼은 필요 시 P1+로 추가한다.

```java
@Entity
@Table(name = "app_user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppUser extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true, columnDefinition = "uuid")
  private UUID publicId;

  @Column(name = "kakao_id", nullable = false, unique = true, length = 64)
  private String kakaoId;

  @Column(name = "email")
  private String email;

  @Builder(access = AccessLevel.PRIVATE)
  private AppUser(UUID publicId, String kakaoId, String email) {
    this.publicId = publicId;
    this.kakaoId = kakaoId;
    this.email = email;
  }

  /** OAuth2Response → 도메인 표준 키(kakao_id) 생성 */
  public static AppUser createFromOAuth(OAuth2Response oAuth2Response) {
    // 예: kakao-12345
    String kakaoId = oAuth2Response.getProvider() + "-" + oAuth2Response.getProviderId();
    return AppUser.builder()
        .publicId(Uuids.next())
        .kakaoId(kakaoId)
        .email(oAuth2Response.getEmail())
        .build();
  }

  public void changeEmail(String email) {
    this.email = email;
  }
}
```

### 4.3 UpbitKey 엔티티(공유 PK + 민감정보 분리)

- 테이블: `upbit_key`
- 키: `owner_id` = PK = FK(app_user.id)
- 값: `byte[]`(암호문)만 저장

```java
@Entity
@Table(name = "upbit_key")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UpbitKey extends BaseEntity {

  @Id
  @Column(name = "owner_id")
  private Long ownerId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  @Column(name = "access_key_enc", nullable = false)
  private byte[] accessKeyEnc;

  @Column(name = "secret_key_enc", nullable = false)
  private byte[] secretKeyEnc;

  @Column(name = "key_version", nullable = false)
  private int keyVersion;


  @Column(name = "rotated_at")
  private Instant rotatedAt;

  public static UpbitKey create(AppUser owner, byte[] accessEnc, byte[] secretEnc, int keyVersion) {
    UpbitKey k = new UpbitKey();
    k.owner = owner;
    k.ownerId = owner.getId();
    k.accessKeyEnc = accessEnc;
    k.secretKeyEnc = secretEnc;
    k.keyVersion = keyVersion;
    return k;
  }

  public void rotate(byte[] newAccessEnc, byte[] newSecretEnc, int newVersion) {
    this.accessKeyEnc = newAccessEnc;
    this.secretKeyEnc = newSecretEnc;
    this.keyVersion = newVersion;
    this.rotatedAt = Instant.now();
  }
}
```

운영 규칙(강제):
- 평문 키는 엔티티/로그/DTO에 절대 남기지 않는다.
- 복호화는 **서비스 계층에서만** 수행하고, 최소 범위에서만 사용한다.

### 4.4 KillSwitch 엔티티(공유 PK, 실행 제어 SoT)

```java
@Entity
@Table(name = "kill_switch")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class KillSwitch extends BaseEntity {

  @Id
  @Column(name = "owner_id")
  private Long ownerId;

  @MapsId
  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_id")
  private AppUser owner;

  @Column(name = "account_enabled", nullable = false)
  private boolean accountEnabled;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "enabled_strategies", columnDefinition = "jsonb", nullable = false)
  private JsonNode enabledStrategies;


  public static KillSwitch init(AppUser owner) {
    KillSwitch ks = new KillSwitch();
    ks.owner = owner;
    ks.ownerId = owner.getId();
    ks.accountEnabled = true;
    ks.enabledStrategies = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
    return ks;
  }

  public void enableAccount() {
    this.accountEnabled = true;
  }

  public void disableAccount() {
    this.accountEnabled = false;
  }
}
```

> `KillSwitch`/`UpbitKey`처럼 1:1 테이블은 부모(`AppUser`)에서 양방향 필드를 두지 않는 편이 안전하다.
> 필요한 경우 Repository로 `findById(ownerId)`로 조회한다.
>
> 시장 단위 실행 중단 상태는 `Position`이 아니라 `MarketState.tradeStatus`에 둔다.  
> 즉, `position.status`는 보유 상태(FLAT/OPEN), `market_state.trade_status`는 실행 상태(ACTIVE/SUSPENDED)다.

---

## 5. DTO(record) 규칙

### 5.1 Request DTO

- Request는 record를 사용한다.
- Builder는 기본 금지(입력은 명시적으로 받는다).
- Bean Validation으로 형식/범위를 검증한다.
- **내부 API JSON은 camelCase가 기본**이다. `@JsonNaming(SnakeCaseStrategy)`는 내부 API DTO에 사용하지 않는다.
  - 예외: 외부 연동 DTO(외부 스펙이 snake_case일 때)만 허용

```java
public record UpdateEmailRequest(
    @Email String email
) {}
```

### 5.2 Response DTO

- Response는 record를 사용한다.
- 필드가 많아 가독성이 떨어지면 `@Builder` + `static from(...)`을 허용한다.
- Response는 엔티티를 직접 들고 있지 않는다(지연로딩/민감정보 누수 방지).
- 내부 API 응답도 camelCase를 기본으로 한다.

```java
@Builder
public record AppUserResponse(
    String userId,     // publicId.toString()
    String kakaoId,
    String email
) {
  public static AppUserResponse from(AppUser u) {
    return AppUserResponse.builder()
        .userId(u.getPublicId())
        .kakaoId(u.getKakaoId())
        .email(u.getEmail())
        .build();
  }
}
```

### 5.3 BotSettingResponse 같은 “대형 응답” 처리 규칙

- 필드가 15개를 넘어가면 응답을 **정책 단위로 쪼개는 것**을 권장한다.
  - `TradingToggle`, `BuyPolicy`, `SellPolicy`, `RiskPolicy` 같은 하위 record 구성
- 단, v2 MVP에서는 빠른 구현이 우선이면 기존처럼 단일 record + builder를 유지해도 된다.

---

## 6. Mapper 규칙

- 간단한 매핑은 DTO 내부 `from(entity)`를 허용한다.
- 복잡해지면 `mapper` 패키지로 분리한다.
- 규모가 커지면 MapStruct 도입을 권장한다(P1+).

---

## 7. Service/Transaction 규칙

- 상태 변경은 서비스에서만 발생한다.
- 트랜잭션 경계:
  - Command: `@Transactional`
  - Query: `@Transactional(readOnly = true)`
- 외부 호출(Upbit/WebPush)은 DB 락/트랜잭션을 길게 잡지 않는다.
  - SoT 변경 + Outbox INSERT는 짧게
  - 외부 호출은 워커/핸들러에서 수행

---

## 8. Repository 규칙

- Repository는 `JpaRepository`를 표준으로 한다.
- 조회 기준:
  - 외부 노출 ID로 찾는 경우 `findByPublicId(String publicId)` 같은 메서드를 제공한다.
  - 내부 조인은 `id(Long)` 사용

예:

```java
interface AppUserRepository extends JpaRepository<AppUser, Long> {
  Optional<AppUser> findByPublicId(String publicId);
  Optional<AppUser> findByKakaoId(String kakaoId);
}
```

복합키 예:

```java
interface StrategyConfigRepository extends JpaRepository<StrategyConfig, StrategyConfigId> {}
```

### 8.1 복잡한 쿼리·조인·성능

기본 CRUD 외에 **조인이 필요하거나 조건이 복잡한 쿼리**는 아래 규칙을 따른다.

| 상황 | 권장 방식 | 비고 |
|------|-----------|------|
| 단일 엔티티 단순 조회 | `JpaRepository` 메서드(`findById`, `findByXxx`) | |
| 연관 엔티티 함께 로딩 | **Fetch Join** | N+1 방지. `@Query("SELECT e FROM Entity e JOIN FETCH e.relation")` |
| 동적 조건·복잡한 조인 | **QueryDSL** | 타입 안전, 조건 조합 용이. `JPAQueryFactory` 사용 |
| 페이징 + 조인 | QueryDSL + `fetchJoin()` + `offset/limit` | count 쿼리 분리 검토 |

규칙:
- **N+1 방지**: 연관 엔티티를 반복 접근할 때는 Fetch Join 또는 `@EntityGraph`로 한 번에 로딩한다.
- **QueryDSL 도입**: `@Query` 문자열이 길어지거나 동적 조건이 많아지면 QueryDSL로 전환한다.
- **성능 검증**: 대량 조회·복잡한 조인 쿼리는 실행 계획(EXPLAIN) 또는 테스트 데이터로 성능을 확인한다.

---

## 9. 컨트롤러 규칙

- 컨트롤러는 DTO만 입출력한다.
- 컨트롤러에서 엔티티를 반환하지 않는다.
- 인증/인가(OWNER-only)는 필터/어노테이션 레벨에서 처리한다.

---


## 10. 테스트/TDD 규칙(강제)

관련 SoT:
- TDD 개발 규칙: `docs/testing/tdd.md`
- **실무 템플릿(구조·예시)**: `docs/testing/backend-tdd-template.md`
- 테스트 전략/게이트: `docs/testing/strategy.md`

규칙:
- 개발은 **RED → GREEN → REFACTOR** 루프를 따른다(테스트 없이 구현부터 시작 금지).
- **시작점은 use case(서비스) 테스트**이다. Controller부터 시작하지 않는다.
- 테스트 코드는 `src/test/java`에서 `src/main/java` 패키지 구조를 그대로 미러링한다.
- 한 테스트는 한 규칙만 검증한다(Given/When/Then 또는 AAA).
- 비즈니스 규칙은 **상태 기반(state-based)** 검증. mock은 controller/external client slice에서만 제한적 사용.

### 10.1 테스트 타입/어노테이션 표준

- Unit(순수): Spring Context 금지  
  - JUnit5 + AssertJ
  - 필요 시 `@ExtendWith(MockitoExtension.class)` 사용(남발 금지)
- Repository/DB 제약: `@DataJpaTest` + **Testcontainers(Postgres)**  
  - H2로 대체하지 않는다(JSONB, SKIP LOCKED, 락 동작 차이)
- UseCase/트랜잭션/Outbox: `@SpringBootTest` + Testcontainers(Postgres)  
  - Outbox publish(INSERT)와 SoT 변경이 **동일 트랜잭션**으로 묶이는지 검증
- Upbit 연동: WireMock 기반 Contract/Stub 테스트  
  - 200/429/418/timeout/5xx 케이스를 고정
- 비동기(워커): `Awaitility` 사용  
  - `Thread.sleep` 금지(Flaky 테스트 원인)

### 10.2 결정적(Deterministic) 테스트 필수

- 시간은 `Clock` 주입을 표준으로 하고, 테스트에서 `Clock.fixed(...)`를 사용한다.
- UUID v7/식별자 생성은 인터페이스로 분리해 테스트에서 고정한다.
- 랜덤 데이터 기반 테스트 금지(재현 불가).

### 10.3 PR/리뷰 기준(테스트 관점)

- 신규 기능/버그 수정 PR은 최소 1개 이상의 테스트를 포함한다.
- P0 영역(주문/멱등/429/418/Outbox)은 **회귀 테스트가 없으면 머지 금지**.


## 11. 체크리스트(리뷰 시 강제)

- [ ] 엔티티에 평문 시크릿/키가 존재하지 않는다.
- [ ] 엔티티에 `@Setter`/`@Data`가 없다.
- [ ] 외부 API는 `publicId`만 노출한다.
- [ ] 복합키/공유PK는 `jpa-mapping.md` 패턴을 따른다.
- [ ] 대형 DTO는 최소한 mapper 분리 또는 하위 record로 구조화했다.
