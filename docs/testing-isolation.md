# 테스트 데이터 격리 가이드

## 개요

테스트 간 데이터 격리는 테스트의 신뢰성과 재현성을 보장하는 핵심 요소입니다.

---

## Testcontainers 기본 개념

### Testcontainers란?

Testcontainers는 Docker 컨테이너를 프로그래밍 방식으로 관리하는 Java/Kotlin 라이브러리입니다.
테스트 실행 시 실제 데이터베이스, 메시지 브로커 등을 Docker 컨테이너로 띄워 테스트합니다.

```
[테스트 실행]
     │
     ├─ Testcontainers가 Docker 컨테이너 시작
     │   └─ PostgreSQL, Redis, Kafka 등
     │
     ├─ 테스트 코드 실행
     │   └─ 실제 DB에 연결하여 테스트
     │
     └─ 테스트 종료 시 컨테이너 정리
```

### 왜 Testcontainers를 사용하는가?

#### H2 인메모리 DB의 한계

| 문제 | 설명 |
|------|------|
| SQL 방언 차이 | H2와 PostgreSQL의 SQL 문법이 다름 |
| 기능 차이 | PostgreSQL 전용 기능(JSONB, Array 등) 테스트 불가 |
| 트랜잭션 동작 차이 | 격리 수준, 락 동작이 다를 수 있음 |
| 프로덕션 버그 미발견 | 개발 환경에서 통과해도 프로덕션에서 실패 가능 |

```kotlin
// H2에서는 동작하지만 PostgreSQL에서 실패하는 예시
@Query("SELECT * FROM book WHERE data::jsonb @> :filter")  // PostgreSQL 전용 문법
fun findByJsonFilter(filter: String): List<Book>
```

#### Testcontainers의 장점

| 장점 | 설명 |
|------|------|
| Dev/Prod Parity | 프로덕션과 동일한 DB로 테스트 (15-Factor #10) |
| 실제 동작 검증 | DB 벤더별 특성, 제약조건 등 실제 동작 확인 |
| 격리된 환경 | 테스트마다 깨끗한 상태의 컨테이너 사용 가능 |
| CI/CD 친화적 | Docker만 있으면 어디서든 동일하게 실행 |

### Testcontainers 동작 방식

```
[JVM 프로세스]                          [Docker]
     │                                     │
     ├─ 테스트 시작                         │
     │                                     │
     ├─ Testcontainers 초기화 ────────────►├─ 컨테이너 이미지 pull (최초 1회)
     │                                     ├─ 컨테이너 생성 및 시작
     │                                     ├─ 포트 매핑 (랜덤 포트)
     │◄────────────────────────────────────┤
     │                                     │
     ├─ 연결 정보 획득                      │
     │   (host, port, username, password)  │
     │                                     │
     ├─ DataSource 설정 ──────────────────►├─ DB 연결
     │                                     │
     ├─ 테스트 실행                         │
     │                                     │
     └─ 테스트 종료 ──────────────────────►└─ 컨테이너 정리 (설정에 따라)
```

---

## Testcontainers 등록 방법과 생명주기

### 1. JDBC URL 방식 (현재 프로젝트에서 사용)

가장 간단한 방식. JDBC URL에 `tc:` 접두사를 붙여 자동으로 컨테이너를 관리합니다.

```yaml
# application-integration.yml
spring:
  datasource:
    url: jdbc:tc:postgresql:14.4:///testdb
```

#### 생명주기

```
[Gradle 테스트 실행: ./gradlew test]
     │
     ├─ 첫 번째 테스트 클래스 로드
     │   └─ DataSource 초기화 시 컨테이너 시작
     │
     ├─ CatalogServiceApplicationTests 실행 ──┐
     ├─ BookRepositoryJdbcTests 실행 ─────────┼─ 같은 컨테이너 공유
     ├─ BookValidationTests 실행 ─────────────┘
     │
     └─ 모든 테스트 완료
         └─ JVM 종료 시 컨테이너 자동 정리
```

| 특징 | 설명 |
|------|------|
| 컨테이너 수 | JVM당 1개 |
| 공유 범위 | 같은 Gradle 테스트 실행 내 모든 테스트 클래스 |
| 장점 | 설정 간단, 컨테이너 재사용으로 빠름 |
| 단점 | 테스트 간 데이터 격리 필요 (`deleteAll()`) |

### 2. @Container + @Testcontainers 방식

테스트 클래스에서 명시적으로 컨테이너를 선언합니다.

```kotlin
@Testcontainers
@DataJdbcTest
class BookRepositoryJdbcTests {

    companion object {
        @Container
        @JvmStatic
        val postgresql = PostgreSQLContainer("postgres:14.4")
    }

    @DynamicPropertySource
    @JvmStatic
    fun configureProperties(registry: DynamicPropertyRegistry) {
        registry.add("spring.datasource.url", postgresql::getJdbcUrl)
        registry.add("spring.datasource.username", postgresql::getUsername)
        registry.add("spring.datasource.password", postgresql::getPassword)
    }
}
```

#### 생명주기 - companion object (static)

```
[테스트 클래스 단위]
     │
     ├─ BookRepositoryJdbcTests 클래스 로드
     │   └─ companion object의 @Container 시작
     │
     ├─ test1() 실행 ──┐
     ├─ test2() 실행 ──┼─ 같은 컨테이너 공유
     ├─ test3() 실행 ──┘
     │
     └─ 클래스의 모든 테스트 완료
         └─ 컨테이너 정리
```

#### 생명주기 - 인스턴스 필드 (non-static)

```kotlin
@Testcontainers
class BookRepositoryJdbcTests {

    @Container  // companion object 아님 = 인스턴스 필드
    val postgresql = PostgreSQLContainer("postgres:14.4")
}
```

```
[테스트 메서드 단위]
     │
     ├─ test1() 시작
     │   ├─ 새 컨테이너 시작
     │   ├─ 테스트 실행
     │   └─ 컨테이너 정리
     │
     ├─ test2() 시작
     │   ├─ 새 컨테이너 시작  ← 매번 새로 시작!
     │   ├─ 테스트 실행
     │   └─ 컨테이너 정리
```

| 선언 위치 | 생명주기 | 사용 시점 |
|-----------|----------|-----------|
| companion object (static) | 클래스당 1개 | 일반적인 경우 (권장) |
| 인스턴스 필드 | 테스트 메서드당 1개 | 완전한 격리 필요 시 (느림) |

### 3. Singleton 패턴 방식

여러 테스트 클래스에서 하나의 컨테이너를 공유합니다.

```kotlin
// 공통 컨테이너 정의
abstract class AbstractIntegrationTest {
    companion object {
        @JvmStatic
        val postgresql: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14.4").apply {
            start()  // 명시적 시작
        }

        init {
            // Spring 프로퍼티 설정
            System.setProperty("spring.datasource.url", postgresql.jdbcUrl)
            System.setProperty("spring.datasource.username", postgresql.username)
            System.setProperty("spring.datasource.password", postgresql.password)
        }
    }
}

// 테스트 클래스에서 상속
class BookRepositoryJdbcTests : AbstractIntegrationTest() { ... }
class CatalogServiceApplicationTests : AbstractIntegrationTest() { ... }
```

#### 생명주기

```
[JVM 전체]
     │
     ├─ AbstractIntegrationTest 클래스 로드 (최초 1회)
     │   └─ companion object init 블록에서 컨테이너 시작
     │
     ├─ BookRepositoryJdbcTests 실행 ──────┐
     ├─ CatalogServiceApplicationTests ────┼─ 모두 같은 컨테이너 공유
     ├─ OtherTests 실행 ───────────────────┘
     │
     └─ JVM 종료
         └─ 컨테이너 자동 정리 (Ryuk 컨테이너가 처리)
```

| 특징 | 설명 |
|------|------|
| 컨테이너 수 | JVM당 1개 |
| 장점 | 명시적 제어, 여러 클래스에서 공유 |
| 단점 | 보일러플레이트 코드, 데이터 격리 필요 |

### 4. @ServiceConnection 방식 (Spring Boot 3.1+)

Spring Boot 3.1부터 지원하는 방식. `@DynamicPropertySource` 없이 자동으로 연결 정보를 설정합니다.

```kotlin
@Testcontainers
@SpringBootTest
class CatalogServiceApplicationTests {

    companion object {
        @Container
        @ServiceConnection  // 자동으로 DataSource 설정!
        @JvmStatic
        val postgresql = PostgreSQLContainer("postgres:14.4")
    }

    // @DynamicPropertySource 불필요
}
```

#### 생명주기

`@Container`의 위치(static/인스턴스)에 따라 동일하게 동작합니다.

| 특징 | 설명 |
|------|------|
| 장점 | 보일러플레이트 감소, Spring Boot와 긴밀한 통합 |
| 지원 서비스 | PostgreSQL, MySQL, Redis, Kafka, RabbitMQ 등 |

---

## 등록 방법별 비교 요약

| 방식 | 설정 복잡도 | 컨테이너 공유 | 생명주기 제어 | 권장 상황 |
|------|-------------|---------------|---------------|-----------|
| JDBC URL (`jdbc:tc:`) | ⭐ 매우 간단 | JVM 전체 | 자동 | 간단한 프로젝트, 빠른 설정 |
| @Container (static) | ⭐⭐ 보통 | 클래스 내 | 클래스 단위 | 클래스별 격리 필요 시 |
| @Container (인스턴스) | ⭐⭐ 보통 | 없음 | 메서드 단위 | 완전한 격리 필요 시 (느림) |
| Singleton 패턴 | ⭐⭐⭐ 복잡 | JVM 전체 | 명시적 | 세밀한 제어 필요 시 |
| @ServiceConnection | ⭐⭐ 보통 | 설정에 따라 | Spring 관리 | Spring Boot 3.1+ 프로젝트 |

---

## Spring의 기본 격리 메커니즘

### @Transactional 롤백

`@DataJdbcTest`, `@DataJpaTest` 등 슬라이스 테스트는 기본적으로 `@Transactional`이 적용됩니다.

```kotlin
@DataJdbcTest  // @Transactional 포함
class BookRepositoryJdbcTests {
    @Test
    fun myTest() {
        // 테스트 실행
        // 테스트 종료 시 자동 롤백 → 데이터 격리
    }
}
```

동작 방식:
1. 테스트 시작 → 트랜잭션 시작
2. 테스트 실행 (INSERT, UPDATE 등)
3. 테스트 종료 → 트랜잭션 롤백 (커밋 안 함)

### RANDOM_PORT 환경에서 롤백이 안 되는 이유

`@SpringBootTest(webEnvironment = RANDOM_PORT)`를 사용하면 `@Transactional` 롤백이 적용되지 않습니다.

```
[테스트 스레드]                    [서버 스레드 (별도)]
     │                                  │
     ├─ 트랜잭션 시작                    │
     │                                  │
     ├─ webTestClient.post() ──────────►├─ HTTP 요청 수신
     │                                  ├─ bookRepository.save()  ← 별도 트랜잭션!
     │                                  ├─ 커밋됨
     │◄──────────────────────────────────┤
     │                                  │
     ├─ 테스트 종료                      │
     ├─ 롤백 (하지만 서버 스레드의 커밋은 이미 완료됨)
```

- 실제 서버가 **별도 스레드**에서 실행됨
- HTTP 요청은 테스트 트랜잭션 **밖에서** 처리됨
- 따라서 저장된 데이터가 롤백되지 않고 남아있음

## Testcontainers 컨테이너 공유 문제

`jdbc:tc:postgresql:14.4:///` URL을 사용하면 같은 Gradle 테스트 실행 내에서 여러 테스트 클래스가 **같은 컨테이너를 공유**합니다.

```
[같은 ./gradlew test 실행]
├── CatalogServiceApplicationTests  ← ISBN "1234567890" 저장 (RANDOM_PORT, 롤백 안 됨)
│
└── BookRepositoryJdbcTests         ← 같은 ISBN으로 저장 시도 → DuplicateKeyException
    └── 같은 PostgreSQL 컨테이너 사용
```

## @BeforeEach + deleteAll() 사용 이유

`@Transactional` 롤백이 있어도 `deleteAll()`이 필요한 경우:

| 상황 | 설명 |
|------|------|
| RANDOM_PORT 환경 | 별도 스레드에서 HTTP 요청 처리되어 롤백 안 됨 |
| 컨테이너 공유 | 다른 테스트 클래스가 남긴 데이터가 있을 수 있음 |
| @Commit 사용 시 | 명시적으로 커밋하는 테스트가 있는 경우 |

```kotlin
@BeforeEach
fun setUp() {
    bookRepository.deleteAll()  // 다른 테스트가 남긴 데이터 정리
}
```

## 현재 프로젝트의 테스트 구조

### CatalogServiceApplicationTests (통합 테스트)

```kotlin
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("integration")
class CatalogServiceApplicationTests(
    @Autowired private val bookRepository: BookRepository,
    @Autowired private val webTestClient: WebTestClient
) {
    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()  // 필수: RANDOM_PORT는 롤백 안 됨
    }
}
```

### BookRepositoryJdbcTests (슬라이스 테스트)

```kotlin
@DataJdbcTest  // @Transactional 포함
@ActiveProfiles("integration")
class BookRepositoryJdbcTests(
    @Autowired private val bookRepository: BookRepository
) {
    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()  // 권장: 컨테이너 공유로 인한 잔여 데이터 정리
    }
}
```

## 병렬 테스트 실행 시 주의점

### 문제점

JUnit 5의 병렬 실행 활성화 시:

```properties
# junit-platform.properties
junit.jupiter.execution.parallel.enabled=true
junit.jupiter.execution.parallel.mode.default=concurrent
```

`deleteAll()`이 다른 테스트의 데이터를 삭제할 수 있음:

```
Test A: save(book1)
Test B: deleteAll()  ← Test A의 book1도 삭제됨!
Test A: findByIsbn(book1.isbn) → null (실패)
```

### 해결 방법

#### 1. 고유한 테스트 데이터 사용 (권장)

```kotlin
@Test
fun `when findByIsbn with existing isbn then return book`() {
    val isbn = "test-${System.nanoTime()}"  // 고유한 ISBN
    val book = Book(isbn = isbn, title = "Title", author = "Author", price = 12.90)
    bookRepository.save(book)

    val foundBook = bookRepository.findByIsbn(isbn)

    assertThat(foundBook?.isbn).isEqualTo(isbn)
}
```

#### 2. 테스트 클래스별 격리

```kotlin
@DataJdbcTest
@Execution(ExecutionMode.SAME_THREAD)  // 같은 클래스 내 순차 실행
class BookRepositoryJdbcTests { ... }
```

#### 3. 테스트별 컨테이너 분리

```kotlin
@Testcontainers
class BookRepositoryJdbcTests {
    companion object {
        @Container  // 클래스마다 새 컨테이너
        val postgresql = PostgreSQLContainer("postgres:14.4")
    }
}
```

## 현재 프로젝트 전략

| 항목 | 설정 |
|------|------|
| 기본 격리 | `@DataJdbcTest`의 `@Transactional` 롤백 |
| 잔여 데이터 정리 | `@BeforeEach` + `deleteAll()` (모든 테스트 클래스) |
| 병렬 실행 | 비활성화 (기본값) |

### 병렬 실행 전환 시 체크리스트

1. [ ] 모든 테스트에서 고유한 테스트 데이터 사용
2. [ ] `deleteAll()` 대신 특정 데이터만 삭제
3. [ ] 공유 상태(static 변수 등) 제거
4. [ ] 테스트 간 의존성 제거

## 실무 조언

- 순차 실행으로 충분하면 병렬화하지 않음 (복잡도 증가)
- 병렬화가 필요하면 고유 데이터 방식 사용
- CI/CD에서는 테스트 분할(test sharding)이 더 효과적일 수 있음
- RANDOM_PORT 환경에서는 반드시 `@BeforeEach`로 데이터 정리
