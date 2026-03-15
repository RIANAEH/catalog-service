package org.polarbookshop.catalogservice.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

/**
 * [Kotlin - data class]
 * 데이터 보관 목적의 클래스를 간결하게 정의
 * 자동 생성되는 메서드:
 * - equals() / hashCode(): 모든 프로퍼티 기반 동등성 비교
 * - toString(): "Book(isbn=..., title=..., ...)" 형태
 * - copy(): 일부 프로퍼티만 변경한 복사본 생성
 * - componentN(): 구조 분해 선언 지원 (val (isbn, title) = book)
 *
 * Java에서 동일한 기능 구현 시:
 * - 모든 필드, 생성자, getter, equals, hashCode, toString 직접 작성
 * - 또는 Lombok @Data 사용
 *
 * [클라우드 네이티브 스프링 - 도메인 모델]
 * 도메인 주도 설계(DDD)의 엔티티 또는 값 객체
 * - 비즈니스 로직의 핵심 개념을 표현
 * - 불변 객체로 설계하여 스레드 안전성 확보
 *
 * [실무 조언]
 * - data class는 불변(immutable)으로 설계 권장 (val 사용)
 * - 변경이 필요하면 copy() 메서드로 새 인스턴스 생성
 * - JPA 엔티티로 사용 시에는 주의 필요 (기본 생성자, var 필요)
 */
data class Book(

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
    val price: Double
)
