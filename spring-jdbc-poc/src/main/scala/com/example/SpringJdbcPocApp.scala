package com.example

import org.springframework.boot.{ApplicationRunner, SpringApplication}
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.data.jdbc.core.mapping.AggregateReference

@SpringBootApplication
class SpringJdbcPocApp:

  @Bean
  def runner(
      people:    PersonRepository,
      depts:     DepartmentRepository,
      streaming: StreamingService
  ): ApplicationRunner = _ =>

    def banner(s: String): Unit = println(s"\n=== $s ===")

    // ── Departments (separate aggregate root, referenced by AggregateReference) ──
    banner("departments")
    val eng = depts.save(Department.newDepartment("Engineering"))
    val mkt = depts.save(Department.newDepartment("Marketing"))
    println(s"saved $eng, $mkt")

    // ── Save a Person aggregate that exercises every mapping feature ────────────
    banner("save a fully-loaded Person aggregate")
    val ada = people.save(
      Person.newPerson(
        name       = "  Ada Lovelace  ",                             // BeforeConvertCallback trims
        age        = 36,
        email      = Email("ada@example.com"),                       // custom converter
        department = AggregateReference.to(eng.id),                  // FK by id only
        addresses  = Set(
          Address("1 Analytical Way", "London", "SW1"),
          Address("Ockham Park",      "Surrey", "GU23")
        ),
        phones     = Map(                                            // Map child collection
          "home"   -> Phone("+44 20 7946 0000"),
          "office" -> Phone("+44 20 7946 0010")
        ),
        hobbies    = List(Hobby("Mathematics"), Hobby("Poetry")),    // ordered List child
        contact    = ContactInfo("+44 20 1234 0000", "https://ada.example") // @Embedded
      )
    )

    println(s"name (post-callback): [${ada.name}]")
    println(s"version (initial):    ${ada.version}")
    println(s"createdAt:            ${ada.createdAt}")
    println(s"createdBy:            ${ada.createdBy}")
    println(s"email type:           ${ada.email.getClass.getSimpleName}, value: ${ada.email}")
    println(s"department FK id:     ${ada.department.getId}")
    println(s"contact (embedded):   ${ada.contact}")
    println(s"addresses:            ${ada.addressesScala.size}")
    println(s"phones:               ${ada.phonesScala}")
    println(s"hobbies (ordered):    ${ada.hobbiesScala.map(_.name).mkString(" → ")}")

    people.save(Person.newPerson("Charles Babbage", 79, Email("charles@example.com"), AggregateReference.to(eng.id)))
    people.save(Person.newPerson("Grace Hopper",    85, Email("grace@example.com"),   AggregateReference.to(mkt.id)))
    people.save(Person.newPerson("Linus Torvalds",  56, Email("linus@example.com")))

    // ── @Modifying + @Query — bulk update inside Spring's transaction ───────────
    banner("@Modifying bulk update")
    val bumped = people.bumpAllAges(1)
    println(s"$bumped rows aged by 1 year")

    // ── Custom repository fragment using raw JdbcTemplate ───────────────────────
    banner("custom repository fragment")
    val renamed = people.renameWithPrefix("Dr. ")
    println(s"$renamed rows renamed (added 'Dr. ' prefix)")

    // ── Paging + sorting via PagingAndSortingRepository ─────────────────────────
    banner("paging & sorting")
    val page0 = people.findAll(PageRequest.of(0, 2, Sort.by("name")))
    println(s"page 0: ${page0.getNumberOfElements}/${page0.getTotalElements} elements across ${page0.getTotalPages} pages")
    page0.forEach(p => println(s"  • ${p.name}, age ${p.age}"))

    val page1 = people.findAll(PageRequest.of(1, 2, Sort.by("name")))
    println(s"page 1:")
    page1.forEach(p => println(s"  • ${p.name}, age ${p.age}"))

    // ── DTO projection ──────────────────────────────────────────────────────────
    banner("DTO projection (@Query → PersonSummary)")
    people.summariesLike("%Lovelace%").forEach(s => println(s"  • $s"))

    // ── Optional return type ────────────────────────────────────────────────────
    banner("findByEmail (Optional return)")
    val byEmail = people.findByEmail("ada@example.com")
    println(s"  found: ${byEmail.isPresent}, name: ${byEmail.map(_.name).orElse("<none>")}")

    // ── Stream<T> consumed inside a @Transactional service ──────────────────────
    banner("streaming results")
    streaming.namesOlderThan(60).foreach(n => println(s"  • $n"))

    // ── @Version optimistic locking conflict ────────────────────────────────────
    banner("optimistic locking (@Version)")
    val a = people.findById(ada.id).get
    val b = people.findById(ada.id).get
    val savedA = people.save(a.copy(name = "Ada (v2)"))
    println(s"first writer wins, version now ${savedA.version}")
    try
      people.save(b.copy(name = "Ada (lost update)"))
      println("(expected a conflict — none thrown)")
    catch
      case e: OptimisticLockingFailureException =>
        println(s"conflict caught: ${e.getMessage.linesIterator.next().take(120)}…")

    // ── Aggregate-on-load — children come back from one save() ──────────────────
    banner("re-load aggregate from id")
    val reloaded = people.findById(savedA.id).orElseThrow()
    println(s"reloaded: ${reloaded.name}")
    println(s"  addresses: ${reloaded.addressesScala.map(_.city).mkString(", ")}")
    println(s"  phones:    ${reloaded.phonesScala.keys.mkString(", ")}")
    println(s"  hobbies:   ${reloaded.hobbiesScala.map(_.name).mkString(" → ")}")
    println(s"  contact:   ${reloaded.contact}")
    println(s"  audit:     v=${reloaded.version}, modifiedAt=${reloaded.lastModifiedAt}")

    println()

object SpringJdbcPocApp:
  def main(args: Array[String]): Unit =
    SpringApplication.run(classOf[SpringJdbcPocApp], args*)
