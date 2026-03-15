package org.polarbookshop.catalogservice.web

import jakarta.validation.Valid
import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * [클라우드 네이티브 스프링 - REST 컨트롤러]
 * @RestController: @Controller + @ResponseBody
 * - 모든 메서드의 반환값이 JSON으로 직렬화됨
 * - RESTful API 엔드포인트 정의
 *
 * @RequestMapping: 기본 URL 경로 설정
 * - 클래스 레벨: 공통 경로 prefix
 * - 메서드 레벨: 세부 경로
 *
 * [실무 조언]
 * - 컨트롤러는 얇게 유지 (요청/응답 변환만)
 * - 비즈니스 로직은 서비스 계층에 위임
 * - REST API 설계 시 리소스 중심 URL 사용 (/books, /books/{isbn})
 */
@RestController
@RequestMapping("/books")
class BookController(
    private val bookService: BookService
) {

    /**
     * [클라우드 네이티브 스프링 - HTTP 메서드 매핑]
     * @GetMapping: HTTP GET 요청 처리
     * - 리소스 조회에 사용
     * - 멱등성(Idempotent): 여러 번 호출해도 결과 동일
     *
     * [Kotlin - 표현식 본문 함수]
     * 단일 표현식 함수는 = 로 간결하게 작성 가능
     * fun get() = bookService.viewBookList()
     */
    @GetMapping
    fun get(): Iterable<Book> {
        return bookService.viewBookList()
    }

    /**
     * [클라우드 네이티브 스프링 - 경로 변수]
     * @PathVariable: URL 경로의 변수 추출
     * - /books/{isbn} -> isbn 파라미터로 바인딩
     * - RESTful API에서 리소스 식별에 사용
     */
    @GetMapping("/{isbn}")
    fun getByIsbn(@PathVariable isbn: String): Book {
        return bookService.viewBookDetails(isbn)
    }

    /**
     * [클라우드 네이티브 스프링 - 리소스 생성]
     * @PostMapping: HTTP POST 요청 처리
     * - 새 리소스 생성에 사용
     * - 멱등성 없음: 여러 번 호출 시 여러 리소스 생성
     *
     * @ResponseStatus(HttpStatus.CREATED): 201 상태 코드 반환
     * - REST 표준: 리소스 생성 성공 시 201 Created
     *
     * @Valid: Bean Validation 활성화
     * - 요청 본문의 유효성 검증
     * - 실패 시 MethodArgumentNotValidException 발생 -> 400 Bad Request
     *
     * @RequestBody: HTTP 요청 본문을 객체로 역직렬화
     * - JSON -> Book 객체 변환 (Jackson 사용)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun post(@Valid @RequestBody book: Book): Book {
        return bookService.addBookToCatalog(book)
    }

    /**
     * [클라우드 네이티브 스프링 - 리소스 삭제]
     * @DeleteMapping: HTTP DELETE 요청 처리
     * - 리소스 삭제에 사용
     * - 멱등성: 여러 번 호출해도 결과 동일 (이미 삭제된 경우 404)
     *
     * @ResponseStatus(HttpStatus.NO_CONTENT): 204 상태 코드 반환
     * - REST 표준: 삭제 성공 시 본문 없이 204 No Content
     */
    @DeleteMapping("/{isbn}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable isbn: String) {
        bookService.removeBookFromCatalog(isbn)
    }

    /**
     * [클라우드 네이티브 스프링 - 리소스 수정]
     * @PutMapping: HTTP PUT 요청 처리
     * - 리소스 전체 교체에 사용
     * - 멱등성: 여러 번 호출해도 결과 동일
     *
     * [실무 조언]
     * - PUT: 리소스 전체 교체 (모든 필드 필요)
     * - PATCH: 리소스 부분 수정 (변경할 필드만)
     * - 현재 구현은 PUT이지만 일부 필드만 수정하므로 PATCH가 더 적절할 수 있음
     */
    @PutMapping("/{isbn}")
    fun put(@PathVariable isbn: String, @Valid @RequestBody book: Book): Book {
        return bookService.editBookDetails(isbn, book)
    }
}
