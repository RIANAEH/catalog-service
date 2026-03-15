package org.polarbookshop.catalogservice.domain

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive

data class Book(

    // @field:NotBlank: 데이터 클래스의 프로퍼티에 적용되는 어노테이션
    // @NotBlank: 일반적으로 클래스의 필드에 적용되는 어노테이션
    @field:NotBlank(message = "ISBN must not be blank")
    @field:Pattern(
        regexp = "^[0-9]{10}([0-9]{3})?$",
        message = "ISBN format must be valid"
    )
    val isbn: String,

    @field:NotBlank(message = "Title must not be blank")
    val title: String,

    @field:NotBlank(message = "Author must not be blank")
    val author: String,

    @field:NotNull(message = "Price must not be null")
    @field:Positive(message = "Price must be a positive value")
    val price: Double
)
