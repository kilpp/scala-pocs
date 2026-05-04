package com.example

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
trait DepartmentRepository extends CrudRepository[Department, java.lang.Long]
