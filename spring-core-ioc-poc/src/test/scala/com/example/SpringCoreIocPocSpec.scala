package com.example

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.context.annotation.AnnotationConfigApplicationContext

class SpringCoreIocPocSpec extends AnyFunSpec with Matchers:

  describe("AnnotationConfigApplicationContext with AppConfig") {

    it("wires Greeter with the @Primary english provider by default") {
      val ctx = AnnotationConfigApplicationContext(classOf[AppConfig])
      try
        val greeter = ctx.getBean(classOf[Greeter])
        greeter.greet("World") shouldBe "Hello, World"
      finally ctx.close()
    }

    it("resolves @Qualifier('english') and wraps it in ShoutingMessageProvider") {
      val ctx = AnnotationConfigApplicationContext(classOf[AppConfig])
      try
        val shouter = ctx.getBean("shouter", classOf[MessageProvider])
        shouter.getClass.getSimpleName shouldBe "ShoutingMessageProvider"
        shouter.message shouldBe "HELLO!"
      finally ctx.close()
    }

    it("excludes @Profile('pt') beans when the profile is not active") {
      val ctx = AnnotationConfigApplicationContext(classOf[AppConfig])
      try
        ctx.containsBean("portuguese") shouldBe false
      finally ctx.close()
    }

    it("activates @Profile('pt') beans when the profile is set") {
      val ctx = AnnotationConfigApplicationContext()
      ctx.getEnvironment.setActiveProfiles("pt")
      ctx.register(classOf[AppConfig])
      ctx.refresh()
      try
        val pt = ctx.getBean("portuguese", classOf[MessageProvider])
        pt.message shouldBe "Olá"
      finally ctx.close()
    }

    it("returns the same instance for a singleton-scoped bean across lookups") {
      val ctx = AnnotationConfigApplicationContext(classOf[AppConfig])
      try
        val a = ctx.getBean(classOf[Greeter])
        val b = ctx.getBean(classOf[Greeter])
        a should be theSameInstanceAs b
      finally ctx.close()
    }
  }
