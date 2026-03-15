package org.polarbookshop.catalogservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.polarbookshop.catalogservice.domain.Book
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * [클라우드 네이티브 스프링 - 통합 테스트]
 * @SpringBootTest: 전체 애플리케이션 컨텍스트를 로드하는 통합 테스트
 * - 모든 빈이 실제로 생성되고 연결됨
 * - 실제 환경과 가장 유사한 테스트
 *
 * webEnvironment = RANDOM_PORT:
 * - 실제 서버를 랜덤 포트에서 시작
 * - 포트 충돌 방지 (CI 환경에서 여러 테스트 동시 실행)
 * - WebTestClient로 실제 HTTP 요청 테스트
 *
 * [실무 조언]
 * - 통합 테스트는 느리므로 핵심 시나리오만 테스트
 * - 단위 테스트 > 슬라이스 테스트 > 통합 테스트 순으로 많이 작성 (테스트 피라미드)
 * - CI/CD 파이프라인에서 통합 테스트는 별도 단계로 분리 고려
 *
 * [Kotlin - 주 생성자 의존성 주입]
 * 테스트 클래스에서도 생성자 주입 사용 가능
 * JUnit 5 + Spring Boot Test가 자동으로 처리
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class CatalogServiceApplicationTests(
    /**
     * [클라우드 네이티브 스프링 - WebTestClient]
     * 비동기/논블로킹 HTTP 클라이언트 (WebFlux 기반)
     * - 실제 HTTP 요청을 보내고 응답 검증
     * - MockMvc보다 실제 환경에 가까운 테스트
     * - 리액티브 스트림 테스트에도 사용 가능
     */
    @Autowired private val webTestClient: WebTestClient
) {

    /**
     * [클라우드 네이티브 스프링 - E2E 테스트]
     * 실제 HTTP 요청으로 전체 흐름 테스트
     * - Controller -> Service -> Repository 전체 경로 검증
     * - JSON 직렬화/역직렬화 포함
     *
     * [Kotlin - 후행 람다 (Trailing Lambda)]
     * .value { actualBook -> ... }
     * 마지막 파라미터가 람다면 괄호 밖으로 뺄 수 있음
     * Java에서는 불가능한 문법
     */
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