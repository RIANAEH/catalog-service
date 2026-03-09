package org.polarbookshop.catalogservice.persistence

import org.polarbookshop.catalogservice.domain.Book
import org.polarbookshop.catalogservice.domain.BookRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryBookRepository : BookRepository {
    private val books = ConcurrentHashMap<String, Book>()

    override fun findAll(): Iterable<Book> {
        return books.values
    }

    override fun findByIsbn(isbn: String): Book? {
        return books[isbn]
    }

    override fun existsByIsbn(isbn: String): Boolean {
        return books.containsKey(isbn)
    }

    override fun save(book: Book): Book {
        books[book.isbn] = book
        return book
    }

    override fun deleteByIsbn(isbn: String) {
        books.remove(isbn)
    }
}