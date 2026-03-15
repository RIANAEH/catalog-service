package org.polarbookshop.catalogservice.domain

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

/**
 * [클라우드 네이티브 스프링 - 단위 테스트]
 * Bean Validation(Jakarta Validation)을 사용한 도메인 객체 유효성 검증 테스트
 * - Spring 컨텍스트 없이 순수 Java/Kotlin 코드로 테스트 (가장 빠른 테스트)
 * - 도메인 모델의 제약 조건을 독립적으로 검증
 *
 * [실무 조언]
 * - 단위 테스트는 테스트 피라미드의 기반 - 가장 많이 작성해야 함
 * - 빠른 피드백 루프로 개발 생산성 향상
 * - 클라우드 환경에서 CI/CD 파이프라인 속도에 직접적 영향
 */
class BookValidationTests {

    /**
     * [Kotlin - companion object]
     * Java의 static 멤버와 유사한 기능을 제공하는 싱글톤 객체
     * - 클래스당 하나만 존재
     * - 클래스 이름으로 직접 접근 가능: BookValidationTests.validator
     *
     * Java 방식:
     * ```java
     * private static Validator validator;
     *
     * @BeforeAll
     * static void setUp() {
     *     validator = Validation.buildDefaultValidatorFactory().getValidator();
     * }
     * ```
     *
     * [Kotlin - lateinit var]
     * 나중에 초기화할 non-null 변수 선언
     * - var만 가능 (val 불가)
     * - primitive 타입 불가
     * - 초기화 전 접근 시 UninitializedPropertyAccessException 발생
     * - Java에서는 null로 초기화하거나 Optional 사용
     *
     * [Kotlin - @JvmStatic]
     * companion object의 메서드를 JVM 바이트코드에서 실제 static 메서드로 생성
     * - JUnit 5의 @BeforeAll은 static 메서드를 요구하므로 필수
     * - Java 코드에서 호출 시에도 static처럼 사용 가능
     */
    companion object {
        private lateinit var validator: Validator

        @BeforeAll
        @JvmStatic
        fun setUp() {
            /**
             * [클라우드 네이티브 스프링 - Validator 생성]
             * Validation.buildDefaultValidatorFactory(): Jakarta Validation 표준 팩토리
             * - Spring 없이도 Bean Validation 사용 가능
             * - Hibernate Validator가 기본 구현체로 사용됨
             *
             * [실무 조언]
             * - Validator는 스레드 안전하므로 재사용 권장
             * - 매 테스트마다 생성하면 성능 저하 발생
             */
            validator = Validation.buildDefaultValidatorFactory().validator
        }
    }

    /**
     * [Kotlin - 백틱(`) 함수명]
     * 공백과 특수문자를 포함한 함수명 작성 가능
     * - 테스트 메서드의 가독성 향상
     * - Java에서는 불가능한 문법
     * - 일반 코드에서는 사용하지 않고 테스트에서만 활용
     */
    @Test
    fun `when all fields are valid then validation succeeds`() {
        val book = Book("1234567890", "Title", "Author", 9.90)
        /**
         * [클라우드 네이티브 스프링 - Bean Validation]
         * validator.validate(): 객체의 모든 제약 조건 검증
         * - 반환값: Set<ConstraintViolation> - 위반 사항 목록
         * - 빈 Set이면 모든 검증 통과
         */
        val violations = validator.validate(book)
        assertThat(violations).isEmpty()
    }

    /**
     * [실무 조언 - 경계값 테스트]
     * 빈 문자열("")은 대표적인 경계값
     * - @NotBlank: null, "", " " 모두 실패
     * - @NotEmpty: null, "" 실패, " "는 통과
     * - @NotNull: null만 실패
     */
    @Test
    fun `when isbn is blank then validation fails`() {
        val book = Book("", "Title", "Author", 9.90)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(2)
        /**
         * [Kotlin - 컬렉션 함수형 처리]
         * map { it.message }: 각 violation에서 message만 추출
         * - Java의 Stream API보다 간결: violations.stream().map(v -> v.getMessage()).collect(...)
         * - it: 람다의 단일 파라미터를 암시적으로 참조
         */
        assertThat(violations.map { it.message }).containsExactlyInAnyOrder(
            "ISBN must not be blank",
            "ISBN format must be valid"
        )
    }

    /**
     * [클라우드 네이티브 스프링 - 정규식 검증]
     * @Pattern: 정규식 패턴 매칭 검증
     * - ISBN-10: 10자리 숫자
     * - ISBN-13: 13자리 숫자
     * - 정규식: ^[0-9]{10}([0-9]{3})?$
     */
    @Test
    fun `when isbn format is invalid then validation fails`() {
        val book = Book("123ABC456", "Title", "Author", 9.90)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(1)
        /**
         * [Kotlin - first()]
         * 컬렉션의 첫 번째 요소 반환
         * - 빈 컬렉션이면 NoSuchElementException 발생
         * - firstOrNull()은 빈 컬렉션에서 null 반환
         */
        assertThat(violations.first().message).isEqualTo("ISBN format must be valid")
    }

    @Test
    fun `when isbn is valid 13 digits then validation succeeds`() {
        val book = Book("1234567890123", "Title", "Author", 9.90)
        val violations = validator.validate(book)
        assertThat(violations).isEmpty()
    }

    @Test
    fun `when title is blank then validation fails`() {
        val book = Book("1234567890", "", "Author", 9.90)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Title must not be blank")
    }

    @Test
    fun `when author is blank then validation fails`() {
        val book = Book("1234567890", "Title", "", 9.90)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Author must not be blank")
    }

    /**
     * [실무 조언 - 숫자 경계값 테스트]
     * @Positive: 0보다 큰 값만 허용
     * - 0은 실패 (0 초과가 아닌 양수만)
     * - @PositiveOrZero: 0 이상 허용
     * - 금액, 수량 등에서 자주 사용
     */
    @Test
    fun `when price is zero then validation fails`() {
        val book = Book("1234567890", "Title", "Author", 0.0)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Price must be a positive value")
    }

    @Test
    fun `when price is negative then validation fails`() {
        val book = Book("1234567890", "Title", "Author", -9.90)
        val violations = validator.validate(book)
        assertThat(violations).hasSize(1)
        assertThat(violations.first().message).isEqualTo("Price must be a positive value")
    }
}
