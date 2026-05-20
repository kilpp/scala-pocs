package com.example

import org.specs2.mutable.Specification
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers

import scala.concurrent.Future
import scala.concurrent.duration.*

/**
 * Async tests — pass an `ExecutionEnv` in the constructor, then
 * use `.await` to assert on Futures. `.await(retries, timeout)`
 * gives you eventually-style polling for free.
 *
 * IMPORTANT: the parameter must be named `ee` (or you provide it
 * implicitly) — `FutureMatchers.await` needs it in scope.
 */
class AsyncSpec(implicit ee: ExecutionEnv) extends Specification with FutureMatchers:

  "Future matchers" >> {

    "await — a successful future returns its value" >> {
      Future.successful(42) must be_==(42).await
    }

    "await with custom timeout" >> {
      val slow = Future {
        Thread.sleep(50)
        "ok"
      }
      slow must be_==("ok").await(retries = 1, timeout = 2.seconds)
    }

    "awaitFor — alias with explicit duration" >> {
      Future.successful(List(1, 2, 3)) must contain(2).awaitFor(1.second)
    }

    "a failed future propagates the exception" >> {
      val bad = Future.failed(new IllegalStateException("nope"))
      bad must throwA[IllegalStateException].await
    }
  }

  "UserService greet" >> {
    val knownRepo = new UserRepository:
      def findById(id: Long): Future[Option[User]] =
        Future.successful(if id == 1L then Some(User(1L, "Ada", "ada@ex.com")) else None)
      def save(u: User): Future[User] = Future.successful(u)

    "greets a known user by name" >> {
      val svc = UserService(knownRepo)
      svc.greet(1L) must be_==("Hello, Ada!").await
    }

    "falls back to 'stranger' for unknown ids" >> {
      val svc = UserService(knownRepo)
      svc.greet(999L) must be_==("Hello, stranger!").await
    }

    "register rejects bad emails synchronously (as a failed Future)" >> {
      val svc = UserService(knownRepo)
      svc.register("Bob", "not-an-email") must throwAn[IllegalArgumentException].await
    }
  }
