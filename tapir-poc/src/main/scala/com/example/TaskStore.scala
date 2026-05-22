package com.example

import cats.effect.{IO, Ref}

final class TaskStore private (state: Ref[IO, (Long, Map[Long, Task])]):
  def list: IO[List[Task]] =
    state.get.map(_._2.values.toList.sortBy(_.id))

  def find(id: Long): IO[Option[Task]] =
    state.get.map(_._2.get(id))

  def create(input: CreateTask): IO[Task] =
    state.modify { case (nextId, tasks) =>
      val task = Task(nextId, input.title, done = false)
      ((nextId + 1, tasks.updated(nextId, task)), task)
    }

  def delete(id: Long): IO[Boolean] =
    state.modify { case (nextId, tasks) =>
      if tasks.contains(id) then ((nextId, tasks.removed(id)), true)
      else ((nextId, tasks), false)
    }

object TaskStore:
  def empty: IO[TaskStore] =
    Ref.of[IO, (Long, Map[Long, Task])]((1L, Map.empty)).map(new TaskStore(_))
