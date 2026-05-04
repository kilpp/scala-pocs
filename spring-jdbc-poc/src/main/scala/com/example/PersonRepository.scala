package com.example

import org.springframework.data.domain.{Page, Pageable}
import org.springframework.data.jdbc.repository.query.{Modifying, Query}
import org.springframework.data.repository.query.Param
import org.springframework.data.repository.{CrudRepository, PagingAndSortingRepository}
import org.springframework.stereotype.Repository

import java.util.{List as JList, Optional}

// DTO projection target for `summariesLike`. Spring Data maps result-set
// columns to the case-class constructor parameters by name.
final case class PersonSummary(name: String, age: Int)

// Custom fragment — implementation supplied by `PersonRepositoryCustomImpl`.
trait PersonRepositoryCustom:
  def renameWithPrefix(prefix: String): Int

@Repository
trait PersonRepository
    extends CrudRepository[Person, java.lang.Long]
    with PagingAndSortingRepository[Person, java.lang.Long]
    with PersonRepositoryCustom:

  // Derived query — Spring parses the method name.
  def findByName(name: String): JList[Person]

  // @Query, named parameter binding.
  @Query("SELECT * FROM people WHERE age >= :minAge ORDER BY age")
  def findOlderThan(@Param("minAge") minAge: Int): JList[Person]

  // @Query returning Optional.
  @Query("SELECT * FROM people WHERE email = :email")
  def findByEmail(@Param("email") email: String): Optional[Person]

  // Modifying DML query — must be invoked inside a transaction.
  @Modifying
  @Query("UPDATE people SET age = age + :delta")
  def bumpAllAges(@Param("delta") delta: Int): Int

  // DTO projection — column names must match constructor parameter names.
  @Query("SELECT name, age FROM people WHERE name LIKE :pattern ORDER BY name")
  def summariesLike(@Param("pattern") pattern: String): JList[PersonSummary]

  // Stream return — caller must keep a transaction open and close the stream.
  def findByAgeGreaterThan(age: Int): java.util.stream.Stream[Person]

  // Paging — overrides PagingAndSortingRepository's signature for clarity.
  override def findAll(pageable: Pageable): Page[Person]
