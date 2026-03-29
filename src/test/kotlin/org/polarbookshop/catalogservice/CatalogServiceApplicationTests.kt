package org.polarbookshop.catalogservice

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
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
 * - 별도 스레드에서 실행되어 @Transactional 롤백이 적용되지 않음
 *
 * [클라우드 네이티브 스프링 - Testcontainers JDBC URL]
 * application-integration.yml에서 jdbc:tc:postgresql:14.4:/// 형식으로 설정
 * - 코드에서 @Container, @ServiceConnection 불필요
 * - JDBC URL만으로 컨테이너 자동 관리
 *
 * [15-Factor #10 Dev/Prod Parity]
 * 개발/테스트 환경을 프로덕션과 최대한 유사하게 유지
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
@ActiveProfiles("integration")
class CatalogServiceApplicationTests(
    @Autowired private val bookRepository: BookRepository,
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
     * [테스트 데이터 격리]
     * RANDOM_PORT 환경에서는 @Transactional 롤백이 적용되지 않음
     * - HTTP 요청이 별도 스레드에서 처리되어 테스트 트랜잭션 밖에서 실행
     * - 명시적으로 데이터 정리 필요
     *
     * @see docs/testing-isolation.md 상세 내용 및 병렬 실행 주의점 참고
     */
    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()
    }

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
        val expectedBook = Book(isbn = "1234567890", title = "Title", author = "Author", price = 9.90, publisher = "Publisher")

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
