package org.polarbookshop.catalogservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.polarbookshop.catalogservice.domain.Book
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest( // 완전한 스프링 웹 어플리케이션 컨텍스트를 로드하여 테스트하는 어노테이션
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT // 테스트 시에 임의의 포트를 사용하여 웹 서버를 시작하도록 설정
)
class CatalogServiceApplicationTests(
    @Autowired private val webTestClient: WebTestClient
) {

    @Test
    fun `when post request then book created`() {
        val expectedBook = Book("1234567890", "Title", "Author", 9.90)

        webTestClient
            .post()
            .uri("/books")
            .bodyValue(expectedBook)
            .exchange()
            .expectStatus().isCreated
            .expectBody(Book::class.java)
            .value { actualBook ->
                assertThat(actualBook).isNotNull
                assertThat(actualBook.isbn).isEqualTo(expectedBook.isbn)
                assertThat(actualBook.title).isEqualTo(expectedBook.title)
                assertThat(actualBook.author).isEqualTo(expectedBook.author)
                assertThat(actualBook.price).isEqualTo(expectedBook.price)
            }
    }
}