package com.example

import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.boot.SpringApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.data.domain.{PageRequest, Sort}
import org.springframework.data.jdbc.core.mapping.AggregateReference

import scala.jdk.CollectionConverters.*

class SpringJdbcPocSpec extends AnyFlatSpec with Matchers with BeforeAndAfterAll:

  private var ctx: ConfigurableApplicationContext = scala.compiletime.uninitialized
  private def repo: PersonRepository              = ctx.getBean(classOf[PersonRepository])
  private def depts: DepartmentRepository         = ctx.getBean(classOf[DepartmentRepository])

  override def beforeAll(): Unit =
    ctx = SpringApplication.run(classOf[SpringJdbcPocApp])

  override def afterAll(): Unit =
    if ctx ne null then ctx.close()

  "PersonRepository" should "save a new Person and assign an id" in {
    val saved = repo.save(Person.newPerson("Linus Torvalds", 56))
    saved.id should not be null
    repo.findById(saved.id).isPresent shouldBe true
  }

  it should "trim the name via BeforeConvertCallback" in {
    val saved = repo.save(Person.newPerson("  Bjarne Stroustrup  ", 75))
    saved.name shouldBe "Bjarne Stroustrup"
  }

  it should "populate audit fields and version on insert" in {
    val saved = repo.save(Person.newPerson("Ken Thompson", 82))
    saved.version    shouldBe 0L
    saved.createdBy  shouldBe "system"
    saved.createdAt  should not be null
  }

  it should "round-trip nested aggregates (addresses, phones, hobbies, contact)" in {
    val saved = repo.save(
      Person.newPerson(
        name      = "Margaret Hamilton",
        age       = 88,
        addresses = Set(Address("MIT Apollo Lab", "Cambridge", "02139")),
        phones    = Map("work" -> Phone("+1 617 0000")),
        hobbies   = List(Hobby("Software"), Hobby("Mountaineering")),
        contact   = ContactInfo("+1 617 1111", "https://hamilton.example")
      )
    )
    val loaded = repo.findById(saved.id).orElseThrow()
    loaded.addressesScala.map(_.city)   shouldBe Set("Cambridge")
    loaded.phonesScala.keys             shouldBe Set("work")
    loaded.hobbiesScala.map(_.name)     shouldBe List("Software", "Mountaineering")
    loaded.contact.website              shouldBe "https://hamilton.example"
  }

  it should "store and read Email through the custom converter" in {
    val saved  = repo.save(Person.newPerson("Donald Knuth", 87, Email("knuth@cs.example")))
    val loaded = repo.findByEmail("knuth@cs.example").orElseThrow()
    loaded.email shouldBe Email("knuth@cs.example")
  }

  it should "resolve AggregateReference id without loading the target" in {
    val d     = depts.save(Department.newDepartment("Research"))
    val saved = repo.save(
      Person.newPerson("Alan Turing", 41, department = AggregateReference.to(d.id))
    )
    val loaded = repo.findById(saved.id).orElseThrow()
    loaded.department.getId shouldBe d.id
  }

  it should "increment @Version and reject stale writes" in {
    val saved = repo.save(Person.newPerson("Edsger Dijkstra", 72))
    val read1 = repo.findById(saved.id).get
    val read2 = repo.findById(saved.id).get
    val first = repo.save(read1.copy(name = "Edsger D. (v2)"))
    first.version shouldBe (saved.version + 1)
    an [OptimisticLockingFailureException] should be thrownBy repo.save(read2.copy(name = "stale"))
  }

  it should "support derived queries, @Query, and DTO projections" in {
    repo.save(Person.newPerson("Barbara Liskov", 86))
    repo.findByName("Barbara Liskov").asScala.map(_.name)             shouldBe List("Barbara Liskov")
    repo.findOlderThan(80).asScala.map(_.name)                        should contain ("Barbara Liskov")
    repo.summariesLike("%Liskov%").asScala.map(_.name)                shouldBe List("Barbara Liskov")
  }

  it should "page results across PagingAndSortingRepository" in {
    (1 to 5).foreach(i => repo.save(Person.newPerson(s"Pageable $i", 30 + i)))
    val page = repo.findAll(PageRequest.of(0, 3, Sort.by("name")))
    page.getNumberOfElements shouldBe 3
    page.getTotalElements    should be >= 5L
  }

  it should "run @Modifying DML and dispatch through the custom fragment" in {
    val before = repo.save(Person.newPerson("Modifiable Person", 40)).age
    repo.bumpAllAges(1)
    val after  = repo.findByName("Modifiable Person").asScala.head.age
    after shouldBe (before + 1)

    repo.renameWithPrefix("Sir/Madam ")
    repo.findByName("Sir/Madam Modifiable Person").asScala should not be empty
  }
