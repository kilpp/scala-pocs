package com.example

import cats.effect.{IO, Ref}

final class TaskStore(ref: Ref[IO, (Long, Map[Long, Task])]):
  def list: IO[List[Task]] =
    ref.get.map(_._2.values.toList.sortBy(_.id))

  def find(id: Long): IO[Option[Task]] =
    ref.get.map(_._2.get(id))

  def create(input: CreateTask): IO[Task] =
    ref.modify { case (nextId, tasks) =>
      val task = Task(nextId, input.title, done = false)
      ((nextId + 1, tasks.updated(nextId, task)), task)
    }

  def delete(id: Long): IO[Boolean] =
    ref.modify { case (nextId, tasks) =>
      val existed = tasks.contains(id)
      ((nextId, tasks - id), existed)
    }

object TaskStore:
  def empty: IO[TaskStore] =
    Ref.of[IO, (Long, Map[Long, Task])]((1L, Map.empty)).map(new TaskStore(_))
