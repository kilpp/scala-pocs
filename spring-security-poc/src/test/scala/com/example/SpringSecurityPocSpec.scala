package com.example

import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import org.springframework.mock.web.MockServletContext
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.{csrf, user}
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{get, post}
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext

class SpringSecurityPocSpec extends AnyFunSpec with Matchers with BeforeAndAfterAll:

  private var ctx: AnnotationConfigWebApplicationContext = scala.compiletime.uninitialized
  private var mockMvc: MockMvc = scala.compiletime.uninitialized

  override def beforeAll(): Unit =
    ctx = AnnotationConfigWebApplicationContext()
    ctx.register(classOf[WebConfig], classOf[WebSecurityConfig])
    ctx.setServletContext(MockServletContext())
    ctx.refresh()
    val builder = MockMvcBuilders.webAppContextSetup(ctx)
    builder.apply(springSecurity())
    mockMvc = builder.build()

  override def afterAll(): Unit =
    if ctx != null then ctx.close()

  describe("authorizeHttpRequests") {

    it("permits anyone on /public") {
      mockMvc.perform(get("/public")).andExpect(status().isOk)
    }

    it("returns 401 on /private without credentials") {
      mockMvc.perform(get("/private")).andExpect(status().isUnauthorized)
    }

    it("allows an authenticated user on /private") {
      mockMvc.perform(get("/private").`with`(user("user").roles("USER")))
        .andExpect(status().isOk)
    }

    it("forbids a non-admin on /admin/**") {
      mockMvc.perform(get("/admin/info").`with`(user("user").roles("USER")))
        .andExpect(status().isForbidden)
    }

    it("allows ROLE_ADMIN on /admin/**") {
      mockMvc.perform(get("/admin/info").`with`(user("boss").roles("ADMIN")))
        .andExpect(status().isOk)
    }
  }

  describe("CSRF protection") {

    it("rejects a POST without a CSRF token (403)") {
      mockMvc.perform(post("/messages").content("hi").`with`(user("user").roles("USER")))
        .andExpect(status().isForbidden)
    }

    it("accepts a POST that carries a valid CSRF token") {
      mockMvc.perform(
        post("/messages").content("hi")
          .contentType("text/plain")
          .`with`(user("user").roles("USER"))
          .`with`(csrf())
      ).andExpect(status().isOk)
    }
  }
