package com.example

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

import scala.annotation.meta.field

@Table("department")
final case class Department(
    @(Id @field) id: java.lang.Long,
    name: String
)

object Department:
  def newDepartment(name: String): Department = Department(null, name)
