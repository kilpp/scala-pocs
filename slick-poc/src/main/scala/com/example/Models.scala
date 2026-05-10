package com.example

final case class Author(id: Long, name: String, country: String)

final case class Book(
    id: Long,
    title: String,
    authorId: Long,
    priceCents: Int,
    year: Int
)
