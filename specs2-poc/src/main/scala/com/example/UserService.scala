package com.example

import scala.concurrent.{ExecutionContext, Future}

final case class User(id: Long, name: String, email: String)

trait UserRepository:
  def findById(id: Long): Future[Option[User]]
  def save(user: User): Future[User]

final class UserService(repo: UserRepository)(using ExecutionContext):
  def greet(id: Long): Future[String] =
    repo.findById(id).map:
      case Some(u) => s"Hello, ${u.name}!"
      case None    => "Hello, stranger!"

  def register(name: String, email: String): Future[User] =
    if !email.contains("@") then
      Future.failed(new IllegalArgumentException(s"invalid email: $email"))
    else
      repo.save(User(id = 0L, name = name, email = email))
