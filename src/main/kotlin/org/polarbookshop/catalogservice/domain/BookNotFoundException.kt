package org.polarbookshop.catalogservice.domain

class BookNotFoundException(isbn: String) : RuntimeException("The book with ISBN $isbn was not found in the catalog")
