package org.polarbookshop.catalogservice.persistence

import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

/**
 * [클라우드 네이티브 스프링 - 인메모리 리포지토리]
 * @Repository: 데이터 접근 계층 컴포넌트
 * - @Component의 특수화된 형태
 * - 예외 변환 기능 제공 (DataAccessException으로 변환)
 *
 * [실무 조언]
 * - 인메모리 구현은 개발/테스트 단계에서 유용
 * - 프로덕션에서는 실제 DB 구현체로 교체
 * - 애플리케이션 재시작 시 데이터 손실됨
 * - 클라우드 환경에서는 상태를 외부 저장소에 보관 (Stateless 원칙)
 *
 * [Kotlin - 인터페이스 구현]
 * 콜론(:)으로 인터페이스 구현 선언
 * Java: implements BookRepository
 * Kotlin: : BookRepository
 */
@Repository
class InMemoryBookRepository : BookRepository {

    /**
     * [Kotlin - 프로퍼티 초기화]
     * 선언과 동시에 초기화
     * private val: 불변 private 필드 + getter 자동 생성
     *
     * [클라우드 네이티브 스프링 - 동시성 처리]
     * ConcurrentHashMap: 스레드 안전한 Map 구현
     * - 여러 요청이 동시에 접근해도 데이터 정합성 보장
     * - 클라우드 환경에서 다중 스레드 처리 필수
     * - 일반 HashMap 사용 시 동시성 문제 발생 가능
     */
    private val books = ConcurrentHashMap<String, Book>()

    /**
     * [Kotlin - override 키워드]
     * 인터페이스/부모 클래스의 메서드 재정의 시 필수
     * Java의 @Override와 유사하지만 필수 키워드
     * - 컴파일 타임에 오버라이드 검증
     * - 오타로 인한 버그 방지
     */
    override fun findAll(): Iterable<Book> {
        return books.values
    }

    /**
     * [Kotlin - Map 접근 연산자]
     * books[isbn]: books.get(isbn)과 동일
     * - 연산자 오버로딩으로 간결한 문법 제공
     * - 키가 없으면 null 반환
     */
    override fun findByIsbn(isbn: String): Book? {
        return books[isbn]
    }

    override fun existsByIsbn(isbn: String): Boolean {
        return books.containsKey(isbn)
    }

    /**
     * [Kotlin - Map 할당 연산자]
     * books[book.isbn] = book: books.put(book.isbn, book)과 동일
     */
    override fun save(book: Book): Book {
        books[book.isbn] = book
        return book
    }

    override fun deleteByIsbn(isbn: String) {
        books.remove(isbn)
    }
}