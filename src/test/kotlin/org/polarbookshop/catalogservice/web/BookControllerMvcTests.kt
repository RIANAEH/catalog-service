package org.polarbookshop.catalogservice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookNotFoundException
import org.polarbookshop.catalogservice.domain.BookService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * [클라우드 네이티브 스프링 - 슬라이스 테스트]
 * @WebMvcTest: 웹 레이어만 로드하는 슬라이스 테스트 어노테이션
 * - 전체 애플리케이션 컨텍스트 대신 MVC 관련 빈만 로드하여 테스트 속도 향상
 * - @Controller, @ControllerAdvice, @JsonComponent, Filter 등만 스캔
 * - @Service, @Repository 등은 로드되지 않으므로 Mock 처리 필요
 *
 * [실무 조언]
 * - 슬라이스 테스트는 단위 테스트와 통합 테스트의 중간 지점
 * - 컨트롤러 로직만 빠르게 검증할 때 유용 (전체 컨텍스트 로드 대비 5~10배 빠름)
 * - 클라우드 환경에서는 빠른 피드백 루프가 중요하므로 슬라이스 테스트 적극 활용 권장
 *
 * [Kotlin - 주 생성자에서 의존성 주입]
 * Java에서는 필드 주입 또는 생성자를 별도로 작성해야 하지만,
 * Kotlin은 주 생성자(primary constructor)에서 바로 의존성 주입 가능
 *
 * Java 방식:
 * ```java
 * @Autowired private MockMvc mockMvc;
 * @Autowired private ObjectMapper objectMapper;
 * ```
 */
@WebMvcTest(BookController::class)
class BookControllerMvcTests(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) {

    /**
     * [클라우드 네이티브 스프링 - Mock 빈]
     * @MockitoBean: Spring 컨텍스트에 Mock 객체를 빈으로 등록
     * - @WebMvcTest는 서비스 레이어를 로드하지 않으므로 Mock으로 대체
     * - 외부 의존성(DB, 외부 API 등)을 격리하여 테스트 안정성 확보
     *
     * [Kotlin - lateinit]
     * Java에서는 필드를 null로 초기화하거나 @Autowired 사용
     * Kotlin의 lateinit: non-null 타입이지만 나중에 초기화할 것을 컴파일러에게 약속
     * - var만 가능 (val 불가)
     * - primitive 타입 불가 (Int, Boolean 등)
     * - 초기화 전 접근 시 UninitializedPropertyAccessException 발생
     */
    @MockitoBean
    private lateinit var bookService: BookService

    /**
     * [클라우드 네이티브 스프링 - BDD 스타일 테스트]
     * given().willReturn(): BDD(Behavior-Driven Development) 스타일의 Mock 설정
     * - given: 특정 메서드 호출 시 (전제 조건)
     * - willReturn: 지정된 값을 반환하도록 설정 (기대 결과)
     *
     * [Kotlin - 백틱(`) 함수명]
     * 테스트 메서드명에 공백과 특수문자 사용 가능
     * Java에서는 불가능한 문법 - 가독성 높은 테스트명 작성에 유용
     *
     * [Kotlin - 문자열 템플릿]
     * "$isbn" 또는 "${expression}" 형태로 문자열 내 변수 삽입
     * Java의 String.format() 또는 + 연결보다 간결함
     */
    @Test
    fun `when get book with existing isbn then return book`() {
        val isbn = "1234567890"
        val book = Book(isbn, "Title", "Author", 9.90)
        given(bookService.viewBookDetails(isbn)).willReturn(book)

        /**
         * [클라우드 네이티브 스프링 - MockMvc 검증]
         * mockMvc.perform(): HTTP 요청 시뮬레이션
         * andExpect(): 응답 검증 - 상태코드, JSON 필드 값 등
         * jsonPath(): JSON 응답에서 특정 필드 값을 추출하여 검증 (JsonPath 문법 사용)
         *
         * [실무 조언]
         * - 응답 본문 전체를 비교하기보다 핵심 필드만 검증하는 것이 유지보수에 유리
         * - 필드 추가/변경 시 테스트가 불필요하게 깨지는 것을 방지
         */
        mockMvc.perform(get("/books/$isbn"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.isbn").value(isbn))
            .andExpect(jsonPath("$.title").value("Title"))
            .andExpect(jsonPath("$.author").value("Author"))
            .andExpect(jsonPath("$.price").value(9.90))
    }

    /**
     * [클라우드 네이티브 스프링 - 예외 상황 테스트]
     * willThrow(): Mock 메서드 호출 시 예외를 던지도록 설정
     * - 정상 케이스뿐 아니라 예외 케이스도 반드시 테스트
     * - @ControllerAdvice와 연계하여 일관된 에러 응답 검증
     *
     * [실무 조언]
     * - 404 Not Found는 리소스가 없을 때의 표준 응답
     * - 클라우드 환경에서는 서비스 간 통신이 많으므로 에러 처리가 더욱 중요
     * - Circuit Breaker 패턴과 함께 사용하면 장애 전파 방지 가능
     */
    @Test
    fun `when get book with non-existing isbn then return 404`() {
        val isbn = "1234567890"
        given(bookService.viewBookDetails(isbn)).willThrow(BookNotFoundException(isbn))

        mockMvc.perform(get("/books/$isbn"))
            .andExpect(status().isNotFound)
    }

    /**
     * [클라우드 네이티브 스프링 - POST 요청 테스트]
     * - contentType(): 요청 본문의 미디어 타입 설정 (application/json)
     * - content(): 요청 본문 데이터 설정 (ObjectMapper로 JSON 직렬화)
     * - isCreated: HTTP 201 상태코드 - REST API에서 리소스 생성 성공을 의미
     *
     * [실무 조언]
     * - POST 성공 시 201 Created + Location 헤더 반환이 REST 표준
     * - 멱등성이 없으므로 재시도 로직 구현 시 주의 필요
     */
    @Test
    fun `when post book with valid data then return created`() {
        val book = Book("1234567890", "Title", "Author", 9.90)
        given(bookService.addBookToCatalog(book)).willReturn(book)

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.isbn").value("1234567890"))
    }

    /**
     * [클라우드 네이티브 스프링 - Validation 테스트]
     * - @Valid 어노테이션이 적용된 컨트롤러에서 유효성 검증 실패 시 400 Bad Request 반환
     * - Mock 설정 없이도 Spring MVC의 Validation이 먼저 동작하여 서비스 호출 전에 실패
     * - 잘못된 요청은 서비스 레이어까지 도달하지 않음 (Fail Fast 원칙)
     *
     * [실무 조언]
     * - 입력 검증은 컨트롤러 레이어에서 처리 (Bean Validation)
     * - 비즈니스 검증은 서비스 레이어에서 처리
     * - 클라이언트에게 명확한 에러 메시지 제공이 중요 (어떤 필드가 왜 잘못됐는지)
     */
    @Test
    fun `when post book with invalid data then return 400`() {
        val invalidBook = Book("invalid", "", "", -1.0)

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidBook))
        )
            .andExpect(status().isBadRequest)
    }
}
