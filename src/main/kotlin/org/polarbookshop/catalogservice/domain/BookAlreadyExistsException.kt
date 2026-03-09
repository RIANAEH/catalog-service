package org.polarbookshop.catalogservice.domain

class BookAlreadyExistsException(isbn: String) : RuntimeException("The bokk with ISBN $isbn already exists in the catalog")