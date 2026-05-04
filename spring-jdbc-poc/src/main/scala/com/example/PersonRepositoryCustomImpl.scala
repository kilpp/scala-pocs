package com.example

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

// Spring Data finds this fragment impl by name: <FragmentInterface>Impl.
// Annotated @Component so it's also a managed bean (any bean of the right
// shape would do — the naming convention is what wires it into the repo).
@Component
class PersonRepositoryCustomImpl(jdbc: JdbcTemplate) extends PersonRepositoryCustom:
  override def renameWithPrefix(prefix: String): Int =
    jdbc.update("UPDATE people SET name = CONCAT(?, name)", prefix)
