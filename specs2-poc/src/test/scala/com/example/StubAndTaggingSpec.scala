package com.example

import org.specs2.mutable.Specification

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.*

/**
 * Test doubles + tagging.
 *
 * NOTE on mocks: specs2 5.x for Scala 3 does NOT publish a `specs2-mock`
 * artifact — the old Mockito integration is Scala-2-only. The recommended
 * options on Scala 3 are:
 *   1. Hand-roll a stub by implementing the trait (shown below). Cleanest
 *      when the trait is small and you only need recording, not matchers.
 *   2. Use mockito-scala-3 or the raw mockito-core API as a separate dep.
 *      Skipped here to keep the POC dependency-light.
 *
 * Tagging — attach tags to examples and then filter from the CLI:
 *   sbt> testOnly *StubAndTaggingSpec* -- include slow
 *   sbt> testOnly *StubAndTaggingSpec* -- exclude slow,wip
 */
class StubAndTaggingSpec extends Specification:

  /** Recording stub: captures every call and lets the test prime responses. */
  final class RecordingUserRepo(seed: Map[Long, User] = Map.empty) extends UserRepository:
    val findCalls: mutable.ArrayBuffer[Long]   = mutable.ArrayBuffer.empty
    val saveCalls: mutable.ArrayBuffer[User]   = mutable.ArrayBuffer.empty
    private var nextId: Long                   = 100L
    private val store: mutable.Map[Long, User] = mutable.Map.from(seed)

    def findById(id: Long): Future[Option[User]] =
      findCalls += id
      Future.successful(store.get(id))

    def save(u: User): Future[User] =
      saveCalls += u
      val assigned = if u.id == 0L then { nextId += 1; u.copy(id = nextId) } else u
      store(assigned.id) = assigned
      Future.successful(assigned)

  "UserService with a hand-rolled stub" >> {

    "greet calls findById exactly once with the requested id" >> {
      val repo = RecordingUserRepo(Map(1L -> User(1L, "Ada", "ada@ex.com")))
      val svc  = UserService(repo)

      Await.result(svc.greet(1L), 1.second) must_== "Hello, Ada!"
      repo.findCalls.toList must_== List(1L)
      repo.saveCalls must beEmpty
    }

    "register persists the user and assigns a fresh id" >> {
      val repo = RecordingUserRepo()
      val svc  = UserService(repo)

      val saved = Await.result(svc.register("Linus", "l@kernel.org"), 1.second)
      saved.id must be_>(0L)
      saved.name must_== "Linus"
      repo.saveCalls.size must_== 1
      repo.saveCalls.head.id must_== 0L // service forwarded the unsaved record
    }

    "register short-circuits on invalid email — repo is never called" >> {
      val repo = RecordingUserRepo()
      val svc  = UserService(repo)

      Await.result(svc.register("Bob", "bad").failed, 1.second) must
        haveClass[IllegalArgumentException]
      repo.saveCalls must beEmpty
      repo.findCalls must beEmpty
    }
  }

  // -------- Tagging --------------------------------------------------------
  // In specs2 mutable, a top-level `tag("name", ...)` fragment tags the next
  // example. Filter at the command line with:
  //   sbt> testOnly *StubAndTaggingSpec* -- include slow
  //   sbt> testOnly *StubAndTaggingSpec* -- exclude slow,wip

  tag("fast")
  "tagged fast: a quick arithmetic check" >> {
    Calculator().add(1, 1) must beEqualTo(2)
  }

  tag("slow")
  "tagged slow: pretend-expensive computation" >> {
    Thread.sleep(10)
    Calculator().add(2, 2) must beEqualTo(4)
  }

  tag("wip")
  "tagged wip: a work-in-progress check we want to isolate" >> {
    Calculator().isEven(8) must beTrue
  }
