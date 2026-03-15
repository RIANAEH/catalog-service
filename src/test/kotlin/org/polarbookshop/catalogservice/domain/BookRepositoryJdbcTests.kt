package org.polarbookshop.catalogservice.domain

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.polarbookshop.catalogservice.config.DataConfig

/**
 * [클라우드 네이티브 스프링 - 데이터 슬라이스 테스트]
 * @DataJdbcTest: Spring Data JDBC 관련 빈만 로드하는 슬라이스 테스트
 * - Repository, JdbcTemplate, DataSource 등만 로드
 * - 웹 레이어, 서비스 레이어는 로드하지 않음
 * - 각 테스트 후 자동 롤백 (트랜잭션)
 *
 * [클라우드 네이티브 스프링 - Testcontainers JDBC URL]
 * application-integration.yml에서 jdbc:tc:postgresql:14.4:/// 형식으로 설정
 * - 코드에서 @Container, @ServiceConnection 불필요
 * - JDBC URL만으로 컨테이너 자동 관리
 *
 * [15-Factor #10 Dev/Prod Parity]
 * 실제 PostgreSQL 컨테이너로 테스트하여 프로덕션 환경과 일치
 * - H2 같은 인메모리 DB 대신 실제 DB 사용
 * - DB 벤더별 SQL 차이로 인한 버그 방지
 *
 * @AutoConfigureTestDatabase(replace = NONE): 테스트용 DB 자동 교체 비활성화
 * - Testcontainers의 PostgreSQL 사용을 위해 필요
 */
@DataJdbcTest
@Import(DataConfig::class)
@ActiveProfiles("integration")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryJdbcTests(
    @Autowired private val bookRepository: BookRepository
) {

    /**
     * [테스트 데이터 격리]
     * Testcontainers 재사용 시 이전 실행의 잔여 데이터 정리
     * - @DataJdbcTest의 @Transactional 롤백과 함께 사용
     * - 병렬 테스트 실행 시에는 고유 데이터 방식 권장
     *
     * @see docs/testing-isolation.md 상세 내용 및 병렬 실행 주의점 참고
     */
    @BeforeEach
    fun setUp() {
        bookRepository.deleteAll()
    }

    @Test
    fun `when findByIsbn with existing isbn then return book`() {
        val isbn = "1234567890"
        val book = Book(isbn = isbn, title = "Title", author = "Author", price = 12.90)
        bookRepository.save(book)

        val foundBook = bookRepository.findByIsbn(isbn)

        assertThat(foundBook).isNotNull
        assertThat(foundBook?.isbn).isEqualTo(isbn)
    }

    @Test
    fun `when findByIsbn with non-existing isbn then return null`() {
        val foundBook = bookRepository.findByIsbn("9999999999")

        assertThat(foundBook).isNull()
    }

    @Test
    fun `when existsByIsbn with existing isbn then return true`() {
        val isbn = "1234567891"
        val book = Book(isbn = isbn, title = "Title", author = "Author", price = 9.90)
        bookRepository.save(book)

        val exists = bookRepository.existsByIsbn(isbn)

        assertThat(exists).isTrue()
    }

    @Test
    fun `when existsByIsbn with non-existing isbn then return false`() {
        val exists = bookRepository.existsByIsbn("9999999999")

        assertThat(exists).isFalse()
    }

    @Test
    fun `when deleteByIsbn then book is removed`() {
        val isbn = "1234567892"
        val book = Book(isbn = isbn, title = "Title", author = "Author", price = 9.90)
        bookRepository.save(book)

        bookRepository.deleteByIsbn(isbn)

        assertThat(bookRepository.findByIsbn(isbn)).isNull()
    }
}
