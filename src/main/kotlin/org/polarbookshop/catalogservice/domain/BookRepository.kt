package org.polarbookshop.catalogservice.domain

/**
 * [클라우드 네이티브 스프링 - 리포지토리 패턴]
 * 데이터 접근 계층을 추상화하는 인터페이스
 * - 도메인 계층이 영속성 기술에 의존하지 않도록 분리
 * - 구현체 교체가 용이 (InMemory -> JPA -> R2DBC 등)
 *
 * [실무 조언]
 * - 도메인 패키지에 인터페이스, persistence 패키지에 구현체 배치
 * - 의존성 역전 원칙(DIP) 적용: 고수준 모듈이 저수준 모듈에 의존하지 않음
 * - 테스트 시 Mock 구현체로 쉽게 대체 가능
 *
 * [Kotlin - 인터페이스]
 * Java와 동일하게 interface 키워드 사용
 * - 프로퍼티 선언 가능 (추상 또는 기본 구현)
 * - 기본 메서드 구현 가능 (Java 8+ default 메서드와 유사)
 */
interface BookRepository {

    /**
     * [Kotlin - 함수 선언]
     * fun 키워드로 함수 선언
     * 반환 타입은 콜론(:) 뒤에 명시
     *
     * Java: Iterable<Book> findAll();
     * Kotlin: fun findAll(): Iterable<Book>
     */
    fun findAll(): Iterable<Book>

    /**
     * [Kotlin - Nullable 타입]
     * Book?: null을 허용하는 타입 (찾지 못하면 null 반환)
     * Java에서는 Optional<Book> 또는 @Nullable 어노테이션 사용
     *
     * Kotlin의 null 안전성:
     * - 컴파일 타임에 NPE 가능성 검사
     * - ?. (안전 호출), ?: (엘비스 연산자), !! (강제 언래핑) 제공
     */
    fun findByIsbn(isbn: String): Book?

    fun existsByIsbn(isbn: String): Boolean

    fun save(book: Book): Book

    /**
     * [Kotlin - Unit 반환 타입]
     * 반환값이 없는 함수는 Unit 타입 (생략 가능)
     * Java의 void와 유사하지만, Unit은 실제 객체 (싱글톤)
     */
    fun deleteByIsbn(isbn: String)
}