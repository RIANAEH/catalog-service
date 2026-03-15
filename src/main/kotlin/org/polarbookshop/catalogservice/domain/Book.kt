package org.polarbookshop.catalogservice.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.annotation.Version
import java.time.Instant

/**
 * [Kotlin - data class]
 * 데이터 보관 목적의 클래스를 간결하게 정의
 * 자동 생성되는 메서드:
 * - equals() / hashCode(): 모든 프로퍼티 기반 동등성 비교
 * - toString(): "Book(isbn=..., title=..., ...)" 형태
 * - copy(): 일부 프로퍼티만 변경한 복사본 생성
 * - componentN(): 구조 분해 선언 지원 (val (isbn, title) = book)
 *
 * [Kotlin - 기본값 파라미터로 정적 팩토리 메서드 대체]
 * Java에서는 id, version 없이 Book을 생성하려면 정적 팩토리 메서드 필요:
 * ```java
 * public static Book of(String isbn, String title, String author, Double price) {
 *     return new Book(null, isbn, title, author, price, 0);
 * }
 * ```
 * Kotlin은 기본값(id = null, version = 0) + 명명된 인자로 해결:
 * ```kotlin
 * val book = Book(isbn = "1234567890", title = "Title", author = "Author", price = 9.90)
 * ```
 * - 빌더 패턴, 텔레스코핑 생성자 패턴도 불필요
 *
 * [클라우드 네이티브 스프링 - 도메인 모델]
 * 도메인 주도 설계(DDD)의 엔티티 또는 값 객체
 * - 비즈니스 로직의 핵심 개념을 표현
 * - 불변 객체로 설계하여 스레드 안전성 확보
 *
 * [클라우드 네이티브 스프링 - Spring Data JDBC 엔티티]
 * JPA와 달리 단순한 POJO로 엔티티 정의
 * - @Entity 불필요, @Id만 필수
 * - 기본 생성자 불필요 (Kotlin data class와 잘 맞음)
 *
 * [실무 조언]
 * - data class는 불변(immutable)으로 설계 권장 (val 사용)
 * - 변경이 필요하면 copy() 메서드로 새 인스턴스 생성
 * - JPA 엔티티로 사용 시에는 주의 필요 (기본 생성자, var 필요)
 */
data class Book(

    /**
     * [클라우드 네이티브 스프링 - @Id]
     * Spring Data의 식별자 어노테이션
     * - 엔티티의 기본 키(Primary Key)를 지정
     * - DB에서 자동 생성 시 null 허용 필요
     *
     * [실무 조언 - 대리키(Surrogate Key) vs 자연키(Natural Key)]
     * ISBN이 자연키로 사용 가능하지만 대리키(id)를 사용하는 이유:
     * 1. 자연키 변경 가능성: ISBN 체계 변경, 오류 수정 등으로 변경될 수 있음
     * 2. 성능: Long(8바이트) vs String(가변) - 인덱스, 조인 성능 우수
     * 3. 외래키 단순화: 다른 테이블에서 참조 시 단일 Long 컬럼으로 충분
     * 4. 관심사 분리: 비즈니스 식별자(ISBN)와 기술적 식별자(id) 분리
     *
     * [Kotlin - Nullable 타입과 기본값]
     * Long?: null 허용 타입, = null: 기본값 설정
     * - 새 엔티티 생성 시 id 없이 생성 가능
     * - 저장 후 DB에서 생성된 id가 할당됨
     *
     * Java로 작성했다면:
     * @Id
     * private Long id;  // wrapper 타입으로 null 허용
     */
    @Id
    val id: Long? = null,

    /**
     * [Kotlin - @field: 어노테이션 사용 위치 지정]
     * Kotlin 프로퍼티는 여러 Java 요소로 컴파일됨:
     * - 필드 (backing field)
     * - getter/setter
     * - 생성자 파라미터
     *
     * @field: 접두사로 어노테이션이 필드에 적용되도록 명시
     * Jakarta Validation은 필드의 어노테이션을 읽으므로 필수
     *
     * 다른 사용 위치 지정자:
     * - @get: getter에 적용
     * - @set: setter에 적용
     * - @param: 생성자 파라미터에 적용
     *
     * [클라우드 네이티브 스프링 - Bean Validation]
     * @NotBlank: null, "", " " 모두 거부
     * @Pattern: 정규식 패턴 검증 (ISBN-10: 10자리, ISBN-13: 13자리)
     */
    @field:NotBlank(message = "ISBN must not be blank")
    @field:Pattern(
        regexp = "^[0-9]{10}([0-9]{3})?$",
        message = "ISBN format must be valid"
    )
    val isbn: String,

    @field:NotBlank(message = "Title must not be blank")
    val title: String,

    @field:NotBlank(message = "Author must not be blank")
    val author: String,

    /**
     * [클라우드 네이티브 스프링 - 선택적 필드]
     * 출판사 정보 (선택 입력)
     * - null 허용으로 기존 데이터와의 호환성 유지
     * - Flyway V2 마이그레이션으로 컬럼 추가
     */
    val publisher: String? = null,

    /**
     * [클라우드 네이티브 스프링 - 숫자 검증]
     * @NotNull: null 거부 (Kotlin의 non-null 타입과 별개로 런타임 검증)
     * @Positive: 0보다 큰 양수만 허용
     *
     * [실무 조언]
     * - 금액은 실무에서 BigDecimal 사용 권장 (부동소수점 오차 방지)
     * - Double은 학습/프로토타입 용도로만 사용
     */
    @field:NotNull(message = "Price must not be null")
    @field:Positive(message = "Price must be a positive value")
    val price: Double,

    /**
     * [클라우드 네이티브 스프링 - @Version (낙관적 잠금)]
     * 동시성 제어를 위한 버전 필드
     * - 엔티티 수정 시 자동으로 버전 증가
     * - 동시 수정 시 OptimisticLockingFailureException 발생
     *
     * [15-Factor #8 Concurrency]
     * 수평 확장 환경에서 데이터 일관성 보장
     * - 여러 인스턴스가 동시에 같은 데이터 수정 시 충돌 감지
     * - 비관적 잠금(DB Lock)보다 성능 우수
     *
     * [실무 조언]
     * - 충돌 발생 시 재시도 로직 구현 필요
     * - 충돌이 빈번하면 비관적 잠금 고려
     */
    @Version
    val version: Int = 0,

    /**
     * [클라우드 네이티브 스프링 - @CreatedDate]
     * 엔티티 생성 시점을 자동으로 기록 (@EnableJdbcAuditing 필요)
     * - Instant: UTC 기준 타임스탬프, 분산 시스템에 적합
     *
     * @see docs/spring-data-auditing.md 활용 사례 참고
     */
    @CreatedDate
    val createdDate: Instant? = null,

    /**
     * [클라우드 네이티브 스프링 - @LastModifiedDate]
     * 엔티티 마지막 수정 시점을 자동으로 기록
     *
     * @see docs/spring-data-auditing.md 활용 사례 참고
     */
    @LastModifiedDate
    val lastModifiedDate: Instant? = null
)
