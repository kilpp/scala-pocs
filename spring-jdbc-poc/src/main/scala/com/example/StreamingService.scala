package com.example

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import scala.jdk.CollectionConverters.*

// `Stream<T>` queries hold an open ResultSet. They must be consumed inside
// an active transaction and the stream must be closed. We wrap that here.
@Service
class StreamingService(repo: PersonRepository):

  @Transactional(readOnly = true)
  def namesOlderThan(age: Int): List[String] =
    val stream = repo.findByAgeGreaterThan(age)
    try stream.iterator().asScala.map(p => s"${p.name} (${p.age})").toList
    finally stream.close()
