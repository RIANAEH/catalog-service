# Spring Data Auditing 가이드

## 개요

Spring Data Auditing은 엔티티의 생성/수정 시간 및 생성/수정자를 자동으로 관리하는 기능입니다.

## 설정 방법

```kotlin
@Configuration
@EnableJdbcAuditing  // JDBC용
// @EnableJpaAuditing  // JPA용
class DataConfig
```

## 주요 어노테이션

| 어노테이션 | 설명 |
|-----------|------|
| `@CreatedDate` | 엔티티 생성 시점 자동 기록 |
| `@LastModifiedDate` | 엔티티 수정 시점 자동 갱신 |
| `@CreatedBy` | 생성자 기록 (AuditorAware 구현 필요) |
| `@LastModifiedBy` | 수정자 기록 (AuditorAware 구현 필요) |

## 시간 타입 선택

| 타입 | 특징 | 권장 환경 |
|------|------|----------|
| `Instant` | UTC 기준 타임스탬프, 타임존 독립적 | 분산 시스템, 마이크로서비스 |
| `LocalDateTime` | 타임존 없음, 로컬 시간 | 단일 서버, 단일 타임존 |
| `ZonedDateTime` | 타임존 포함 | 다중 타임존 지원 필요 시 |

## 활용 사례

### 1. HTTP 캐시 (조건부 요청)

```kotlin
@GetMapping("/books/{isbn}")
fun getBook(@PathVariable isbn: String): ResponseEntity<Book> {
    val book = bookService.viewBookDetails(isbn)
    return ResponseEntity.ok()
        .lastModified(book.lastModifiedDate!!)  // Last-Modified 헤더
        .body(book)
}
```

클라이언트 요청 흐름:
1. 첫 요청 → 서버가 `Last-Modified: 2026-03-15T09:00:00Z` 헤더와 함께 응답
2. 재요청 시 `If-Modified-Since: 2026-03-15T09:00:00Z` 헤더 포함
3. 서버가 `lastModifiedDate`와 비교:
   - 변경 없음 → `304 Not Modified` (본문 없이 응답, 네트워크 절약)
   - 변경됨 → `200 OK` + 새 데이터

### 2. 증분 동기화 (Incremental Sync)

마이크로서비스 간 데이터 동기화 시 "특정 시점 이후 변경된 데이터만" 조회:

```kotlin
@Query("SELECT * FROM book WHERE last_modified_date > :since")
fun findModifiedSince(since: Instant): List<Book>
```

### 3. 캐시 무효화 판단

Redis 등 외부 캐시 사용 시:

```kotlin
fun getBookWithCache(isbn: String): Book {
    val cached = redisTemplate.opsForValue().get("book:$isbn")
    val dbBook = bookRepository.findByIsbn(isbn)

    // DB 데이터가 더 최신이면 캐시 갱신
    if (cached == null || dbBook.lastModifiedDate!! > cached.lastModifiedDate!!) {
        redisTemplate.opsForValue().set("book:$isbn", dbBook)
        return dbBook
    }
    return cached
}
```

### 4. 데이터 변경 이력 추적

디버깅, 감사(Audit), 문제 추적에 활용:

```kotlin
// 최근 수정된 도서 조회
@Query("SELECT * FROM book ORDER BY last_modified_date DESC LIMIT 10")
fun findRecentlyModified(): List<Book>
```

## 실무 조언

- 대부분의 엔티티에 `createdDate`, `lastModifiedDate`는 필수로 추가
- 분산 시스템에서는 `Instant` (UTC) 사용 권장
- `createdDate`는 INSERT 후 변경되면 안 됨 (불변)
- `version`과 함께 사용하여 동시성 문제 추적 가능
