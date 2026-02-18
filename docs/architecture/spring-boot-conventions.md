# Spring Boot 코드/클래스 규칙

Status: **Ready for Implementation (v2 MVP)**  
Owner: everbit  
Last updated: 2026-02-17 (Asia/Seoul)

이 문서는 everbit v2 백엔드(Spring Boot)에서 **클래스/패키지/레이어/엔티티/DTO**를 어떤 규칙으로 작성할지 고정한다.

- DB 스키마/제약의 SoT: `docs/architecture/data-model.md`, `docs/db/schema-v2-mvp.sql`
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
    util/              # ULID, 시간, 공용 유틸

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
- 외부 노출/추적용: `public_id char(26)`(ULID) → `String publicId`
  - API/URL/로그에는 `publicId`를 사용한다.
  - `id`는 외부에 노출하지 않는다.

ULID 생성 표준:
- 저장 직전 `@PrePersist`에서 null이면 생성한다.
- ULID 생성기는 thread-safe하게 제공한다(예: ThreadLocal).

```java
public final class Ulids {
  private Ulids() {}
  private static final ThreadLocal<de.huxhorn.sulky.ulid.ULID> TL =
      ThreadLocal.withInitial(de.huxhorn.sulky.ulid.ULID::new);

  public static String next() {
    return TL.get().nextULID();
  }
}
```

### 3.3 시간 컬럼 매핑 규칙

- DB는 `timestamptz(UTC)`다.
- Java 타입은 `Instant`를 표준으로 한다(표시 시 KST 변환).

스키마에 따라 base class를 분리한다.

- `created_at`만 있는 테이블: `CreatedAtEntity`
- `updated_at`만 있는 테이블: `UpdatedAtEntity`
- 둘 다 있는 테이블: `AuditableEntity`

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class CreatedAtEntity {
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class UpdatedAtEntity {
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditableEntity extends CreatedAtEntity {
  @LastModifiedDate
  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
```

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

1) **PK를 ULID String으로 직접 보유**
- v2는 내부 PK를 `Long(identity)`로 고정했다.
- ULID는 `publicId`로 별도 컬럼으로 둔다.

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
public class AppUser extends CreatedAtEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "public_id", nullable = false, unique = true, length = 26, columnDefinition = "char(26)")
  private String publicId;

  @Column(name = "kakao_id", nullable = false, unique = true, length = 64)
  private String kakaoId;

  @Column(name = "email")
  private String email;

  @PrePersist
  void prePersist() {
    if (publicId == null) {
      publicId = Ulids.next();
    }
  }

  @Builder(access = AccessLevel.PRIVATE)
  private AppUser(String kakaoId, String email) {
    this.kakaoId = kakaoId;
    this.email = email;
  }

  /** OAuth2Response → 도메인 표준 키(kakao_id) 생성 */
  public static AppUser createFromOAuth(OAuth2Response oAuth2Response) {
    // 예: kakao-12345
    String kakaoId = oAuth2Response.getProvider() + "-" + oAuth2Response.getProviderId();
    return AppUser.builder()
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
public class UpbitKey {

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

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "rotated_at")
  private Instant rotatedAt;

  public static UpbitKey create(AppUser owner, byte[] accessEnc, byte[] secretEnc, int keyVersion) {
    UpbitKey k = new UpbitKey();
    k.owner = owner;
    k.ownerId = owner.getId();
    k.accessKeyEnc = accessEnc;
    k.secretKeyEnc = secretEnc;
    k.keyVersion = keyVersion;
    k.createdAt = Instant.now();
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
public class KillSwitch {

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

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public static KillSwitch init(AppUser owner) {
    KillSwitch ks = new KillSwitch();
    ks.owner = owner;
    ks.ownerId = owner.getId();
    ks.accountEnabled = true;
    ks.enabledStrategies = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
    ks.updatedAt = Instant.now();
    return ks;
  }

  public void enableAccount() {
    this.accountEnabled = true;
    this.updatedAt = Instant.now();
  }

  public void disableAccount() {
    this.accountEnabled = false;
    this.updatedAt = Instant.now();
  }
}
```

> `KillSwitch`/`UpbitKey`처럼 1:1 테이블은 부모(`AppUser`)에서 양방향 필드를 두지 않는 편이 안전하다.
> 필요한 경우 Repository로 `findById(ownerId)`로 조회한다.

---

## 5. DTO(record) 규칙

### 5.1 Request DTO

- Request는 record를 사용한다.
- Builder는 기본 금지(입력은 명시적으로 받는다).
- Bean Validation으로 형식/범위를 검증한다.

```java
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UpdateEmailRequest(
    @Email String email
) {}
```

### 5.2 Response DTO

- Response는 record를 사용한다.
- 필드가 많아 가독성이 떨어지면 `@Builder` + `static from(...)`을 허용한다.
- Response는 엔티티를 직접 들고 있지 않는다(지연로딩/민감정보 누수 방지).

```java
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AppUserResponse(
    String userId,     // publicId
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

---

## 9. 컨트롤러 규칙

- 컨트롤러는 DTO만 입출력한다.
- 컨트롤러에서 엔티티를 반환하지 않는다.
- 인증/인가(OWNER-only)는 필터/어노테이션 레벨에서 처리한다.

---


## 10. 테스트/TDD 규칙(강제)

관련 SoT:
- TDD 개발 규칙: `docs/testing/tdd.md`
- 테스트 전략/게이트: `docs/testing/strategy.md`

규칙:
- 개발은 **RED → GREEN → REFACTOR** 루프를 따른다(테스트 없이 구현부터 시작 금지).
- 테스트 코드는 `src/test/java`에서 `src/main/java` 패키지 구조를 그대로 미러링한다.
- 한 테스트는 한 규칙만 검증한다(Given/When/Then 또는 AAA).

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
- ULID/식별자 생성은 인터페이스로 분리해 테스트에서 고정한다.
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
