package org.polarbookshop.catalogservice.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.JacksonTester

/**
 * [클라우드 네이티브 스프링 - JSON 슬라이스 테스트]
 * @JsonTest: JSON 직렬화/역직렬화만 테스트하는 슬라이스 테스트
 * - Jackson ObjectMapper와 @JsonComponent만 로드
 * - 웹 레이어나 서비스 레이어는 로드하지 않음
 * - API 응답/요청의 JSON 형식이 올바른지 검증
 *
 * [실무 조언]
 * - API 계약(Contract)을 보장하는 중요한 테스트
 * - 클라이언트와 서버 간 JSON 형식 불일치로 인한 버그 방지
 * - 필드명 변경, 타입 변경 등의 Breaking Change를 조기에 발견
 * - 클라우드 환경에서 마이크로서비스 간 통신 시 특히 중요
 *
 * [Kotlin - 클래스 레벨 @Autowired]
 * Java에서는 필드마다 @Autowired를 붙이지만,
 * Kotlin은 주 생성자에서 한 번에 주입받을 수 있음
 */
@JsonTest
class BookJsonTests(
    @Autowired private val json: JacksonTester<Book>
) {

    /**
     * [클라우드 네이티브 스프링 - 직렬화 테스트]
     * 직렬화(Serialization): 객체 -> JSON 문자열 변환
     * - 서버가 클라이언트에게 응답을 보낼 때 발생
     * - 필드명, 값 형식이 예상대로 출력되는지 검증
     *
     * [Kotlin - val과 불변성]
     * val book: 재할당 불가능한 변수 (Java의 final과 유사)
     * data class의 val 프로퍼티는 getter만 생성됨
     * 불변 객체는 스레드 안전하여 클라우드 환경에서 유리
     */
    @Test
    fun `serialize book to json`() {
        val book = Book(isbn = "1234567890", title = "Title", author = "Author", price = 9.90)

        /**
         * [클라우드 네이티브 스프링 - JacksonTester]
         * write(): 객체를 JSON으로 직렬화
         * hasJsonPathStringValue(): 특정 JSON 경로에 문자열 값이 있는지 검증
         * extractingJsonPathStringValue(): JSON 경로에서 값을 추출하여 검증
         *
         * [실무 조언]
         * - JsonPath 문법으로 중첩된 JSON도 쉽게 검증 가능
         * - $.필드명 형태로 루트 레벨 필드 접근
         * - $.items[0].name 형태로 배열 내 객체 접근
         */
        val jsonContent = json.write(book)

        assertThat(jsonContent).hasJsonPathStringValue("@.isbn")
        assertThat(jsonContent).extractingJsonPathStringValue("@.isbn").isEqualTo("1234567890")
        assertThat(jsonContent).extractingJsonPathStringValue("@.title").isEqualTo("Title")
        assertThat(jsonContent).extractingJsonPathStringValue("@.author").isEqualTo("Author")
        assertThat(jsonContent).extractingJsonPathNumberValue("@.price").isEqualTo(9.90)
    }

    /**
     * [클라우드 네이티브 스프링 - 역직렬화 테스트]
     * 역직렬화(Deserialization): JSON 문자열 -> 객체 변환
     * - 클라이언트가 서버에게 요청을 보낼 때 발생
     * - JSON 형식이 올바르게 객체로 변환되는지 검증
     *
     * [Kotlin - 원시 문자열 (Raw String)]
     * 삼중 따옴표(""")로 여러 줄 문자열 작성 가능
     * 이스케이프 문자 없이 JSON을 그대로 작성할 수 있어 가독성 향상
     * trimIndent(): 공통 들여쓰기 제거
     *
     * Java에서는 문자열 연결이나 텍스트 블록(Java 15+) 사용:
     * ```java
     * String json = """
     *     {"isbn": "1234567890"}
     *     """;
     * ```
     */
    @Test
    fun `deserialize json to book`() {
        val content = """
            {
                "isbn": "1234567890",
                "title": "Title",
                "author": "Author",
                "price": 9.90
            }
        """.trimIndent()

        /**
         * [클라우드 네이티브 스프링 - 역직렬화 검증]
         * parse(): JSON 문자열을 객체로 역직렬화
         * usingRecursiveComparison(): 객체의 모든 필드를 재귀적으로 비교
         *
         * [Kotlin - data class의 equals()]
         * data class는 자동으로 equals() 구현
         * 모든 프로퍼티 값이 같으면 동등한 객체로 판단
         * Java에서는 직접 equals/hashCode를 구현하거나 Lombok 사용
         */
        val parsedBook = json.parse(content)

        assertThat(parsedBook).usingRecursiveComparison()
            .isEqualTo(Book(isbn = "1234567890", title = "Title", author = "Author", price = 9.90))
    }
}
