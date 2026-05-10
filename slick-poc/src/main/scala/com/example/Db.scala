package com.example

import slick.jdbc.H2Profile.api.*

object Db:

  /** In-memory H2 database. `DB_CLOSE_DELAY=-1` keeps the database alive for
    * the lifetime of the JVM so that subsequent `Database.forURL` calls with
    * the same name reattach to the same in-memory store. */
  def memory(name: String): Database =
    Database.forURL(
      url             = s"jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1",
      driver          = "org.h2.Driver",
      keepAliveConnection = true
    )
