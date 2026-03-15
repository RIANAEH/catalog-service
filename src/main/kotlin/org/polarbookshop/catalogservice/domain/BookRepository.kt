package org.polarbookshop.catalogservice.domain

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

/**
 * [클라우드 네이티브 스프링 - Spring Data Repository]
 * CrudRepository를 확장하여 기본 CRUD 메서드 자동 제공
 * - save(), findById(), findAll(), deleteById() 등 기본 제공
 * - 메서드 이름 기반 쿼리 자동 생성 (Query Methods)
 *
 * [Kotlin - 인터페이스 상속]
 * CrudRepository<Book, Long>: 엔티티 타입과 ID 타입을 제네릭으로 지정
 * - Book: 관리할 엔티티 타입
 * - Long: @Id 필드의 타입
 *
 * [실무 조언]
 * - Spring Data가 런타임에 구현체를 자동 생성 (프록시)
 * - 복잡한 쿼리는 @Query 어노테이션으로 직접 작성
 * - 도메인 패키지에 위치시켜 도메인 계층에서 접근
 */
interface BookRepository : CrudRepository<Book, Long> {

    /**
     * [클라우드 네이티브 스프링 - Query Methods]
     * 메서드 이름으로 쿼리 자동 생성
     * - findBy + 필드명: SELECT ... WHERE 필드 = ?
     * - Spring Data가 메서드 이름을 파싱하여 쿼리 생성
     *
     * [Kotlin - Nullable 반환 타입]
     * Book?: 결과가 없으면 null 반환
     * - Optional<Book> 대신 Kotlin의 null 안전성 활용
     */
    fun findByIsbn(isbn: String): Book?

    /**
     * [클라우드 네이티브 스프링 - 존재 여부 확인]
     * existsBy + 필드명: SELECT EXISTS(...)
     * - count 쿼리보다 효율적 (첫 번째 결과만 확인)
     */
    fun existsByIsbn(isbn: String): Boolean

    /**
     * [클라우드 네이티브 스프링 - @Query + @Modifying]
     * 커스텀 DELETE 쿼리 작성
     * - @Modifying: INSERT, UPDATE, DELETE 쿼리임을 명시
     * - @Transactional: 트랜잭션 내에서 실행
     *
     * [실무 조언]
     * - CrudRepository의 deleteById()는 ID 기반 삭제
     * - 비즈니스 키(ISBN) 기반 삭제는 커스텀 쿼리 필요
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM book WHERE isbn = :isbn")
    fun deleteByIsbn(isbn: String)
}
