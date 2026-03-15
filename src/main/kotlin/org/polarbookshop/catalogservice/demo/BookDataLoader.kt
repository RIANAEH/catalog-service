package org.polarbookshop.catalogservice.demo

import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookRepository
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

/**
 * [클라우드 네이티브 스프링 - 테스트 데이터 로더]
 * 개발/테스트 환경에서 초기 데이터를 로드하는 컴포넌트
 *
 * [15-Factor #10 Dev/Prod Parity]
 * - 개발 환경에서 실제와 유사한 데이터로 테스트
 * - 프로파일로 환경별 동작 분리
 *
 * [클라우드 네이티브 스프링 - @Profile]
 * 특정 프로파일이 활성화된 경우에만 빈 등록
 * - 활성화: spring.profiles.active=testdata
 * - 환경 변수: SPRING_PROFILES_ACTIVE=testdata
 * - 여러 프로파일 조합 가능: @Profile("testdata & !prod")
 *
 * [실무 조언]
 * - 프로덕션에서는 절대 테스트 데이터 로더가 실행되지 않도록 주의
 * - 프로파일명을 명확하게 지정 (dev, test, staging, prod 등)
 * - CI/CD 파이프라인에서 프로파일 설정 자동화
 *
 * [Kotlin - 주 생성자 의존성 주입]
 * 클래스 선언부에서 바로 의존성 주입
 * Java의 @Autowired 생성자 주입과 동일하지만 더 간결
 */
@Component
@Profile("testdata")
class BookDataLoader(
    private val bookRepository: BookRepository
) {

    /**
     * [클라우드 네이티브 스프링 - ApplicationReadyEvent]
     * 애플리케이션이 완전히 시작된 후 실행되는 이벤트 리스너
     *
     * 다른 초기화 방법들과 비교:
     * - @PostConstruct: 빈 생성 직후 (다른 빈이 아직 준비 안 됐을 수 있음)
     * - CommandLineRunner: 애플리케이션 컨텍스트 로드 후
     * - ApplicationReadyEvent: 모든 준비 완료 후 (가장 안전)
     *
     * [Kotlin - 단일 표현식이 아닌 함수]
     * 여러 문장이 있는 함수는 중괄호 블록 사용
     */
    @EventListener(ApplicationReadyEvent::class)
    fun loadBookTestData() {
        // 기존 데이터 모두 삭제
        bookRepository.deleteAll()

        val book1 = Book(
            isbn = "1234567891",
            title = "Northern Lights",
            author = "Lyra Silverstar",
            publisher = "Polarsophia",
            price = 9.90
        )
        val book2 = Book(
            isbn = "1234567892",
            title = "Polar Journey",
            author = "Iorek Polarson",
            publisher = "Polarsophia",
            price = 12.90
        )

        /**
         * [클라우드 네이티브 스프링 - CrudRepository.saveAll()]
         * 여러 엔티티를 한 번에 저장
         * - 내부적으로 배치 처리하여 성능 향상
         * - 단일 트랜잭션으로 처리됨
         */
        bookRepository.saveAll(listOf(book1, book2))
    }
}
