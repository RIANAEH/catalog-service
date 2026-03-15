package org.polarbookshop.catalogservice.domain

import org.springframework.stereotype.Service

/**
 * [클라우드 네이티브 스프링 - 서비스 계층]
 * @Service: 비즈니스 로직을 담당하는 서비스 컴포넌트
 * - @Component의 특수화된 형태 (의미론적 구분)
 * - 트랜잭션 경계 설정의 일반적인 위치
 * - 컨트롤러와 리포지토리 사이의 중간 계층
 *
 * [실무 조언]
 * - 서비스는 비즈니스 로직만 담당, 웹/영속성 관심사 분리
 * - 하나의 서비스가 여러 리포지토리를 조합할 수 있음
 * - 마이크로서비스에서는 서비스 간 통신도 이 계층에서 처리
 *
 * [Kotlin - 주 생성자 의존성 주입]
 * 생성자 파라미터로 의존성 선언 시 자동 주입
 * - @Autowired 생략 가능 (Spring 4.3+)
 * - private val로 선언하여 불변 필드로 저장
 *
 * Java 방식:
 * ```java
 * @Service
 * public class BookService {
 *     private final BookRepository bookRepository;
 *
 *     @Autowired
 *     public BookService(BookRepository bookRepository) {
 *         this.bookRepository = bookRepository;
 *     }
 * }
 * ```
 */
@Service
class BookService(
    private val bookRepository: BookRepository
) {

    fun viewBookList(): Iterable<Book> {
        return bookRepository.findAll()
    }

    /**
     * [Kotlin - 엘비스 연산자 (?:)]
     * null이면 우측 표현식 실행
     * - findByIsbn()이 null 반환 시 예외 던짐
     * - Java의 Optional.orElseThrow()와 유사한 패턴
     *
     * Java 방식:
     * ```java
     * return bookRepository.findByIsbn(isbn)
     *     .orElseThrow(() -> new BookNotFoundException(isbn));
     * ```
     */
    fun viewBookDetails(isbn: String): Book {
        return bookRepository.findByIsbn(isbn)
            ?: throw BookNotFoundException(isbn)
    }

    /**
     * [클라우드 네이티브 스프링 - 비즈니스 검증]
     * 입력 검증(@Valid)과 별개로 비즈니스 규칙 검증
     * - 중복 ISBN 체크는 비즈니스 로직
     * - 서비스 계층에서 처리하는 것이 적절
     */
    fun addBookToCatalog(book: Book): Book {
        if (bookRepository.existsByIsbn(book.isbn)) {
            throw BookAlreadyExistsException(book.isbn)
        }
        return bookRepository.save(book)
    }

    fun removeBookFromCatalog(isbn: String) {
        if (!bookRepository.existsByIsbn(isbn)) {
            throw BookNotFoundException(isbn)
        }
        bookRepository.deleteByIsbn(isbn)
    }

    /**
     * [Kotlin - 안전 호출 연산자 (?.) + let]
     * ?.let { }: null이 아닐 때만 블록 실행
     * - it: 람다의 암시적 파라미터 (찾은 Book 객체)
     * - 체이닝으로 null 안전한 코드 작성
     *
     * [Kotlin - copy() 함수]
     * data class의 일부 프로퍼티만 변경한 복사본 생성
     * - 불변 객체 패턴 구현에 유용
     * - 명시하지 않은 프로퍼티(id, version, isbn)는 원본 값 유지
     * - id 유지 → UPDATE 쿼리 실행 (INSERT가 아님)
     * - version 유지 → Spring Data가 낙관적 잠금 검증 및 자동 증가
     *
     * Java에서는 Builder 패턴이나 새 객체 생성 필요
     */
    fun editBookDetails(isbn: String, book: Book): Book {
        return bookRepository.findByIsbn(isbn)?.let {
            bookRepository.save(it.copy(
                title = book.title,
                author = book.author,
                price = book.price
            ))
        } ?: throw BookNotFoundException(isbn)
    }
}
