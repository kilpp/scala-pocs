package com.example

import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.{GetMapping, PostMapping, RequestBody, RestController}

/** A handful of endpoints with deliberately different access rules so the
  * SecurityFilterChain in [[WebSecurityConfig]] has something to enforce:
  *
  *   - `/public`       open to everyone (permitAll)
  *   - `/private`      any authenticated user
  *   - `/admin/info`   ROLE_ADMIN only
  *   - `/csrf`         returns the current CSRF token for this session
  *   - `POST /messages` write endpoint that CSRF protection guards
  */
@RestController
class ApiController:

  @GetMapping(Array("/public"))
  def publicEndpoint(): String = "public: anyone can read this"

  @GetMapping(Array("/private"))
  def privateEndpoint(): String = "private: you are authenticated"

  @GetMapping(Array("/admin/info"))
  def adminEndpoint(): String = "admin: you have ROLE_ADMIN"

  /** Hands the caller the CSRF token Spring stored for this session so a
    * non-browser client can echo it back on the next mutating request.
    */
  @GetMapping(Array("/csrf"))
  def csrf(request: HttpServletRequest): String =
    val token = request.getAttribute(classOf[CsrfToken].getName).asInstanceOf[CsrfToken]
    token.getToken

  /** Mutating endpoint: reachable only with a valid CSRF token (and auth). */
  @PostMapping(Array("/messages"))
  def createMessage(@RequestBody body: String): String =
    s"message accepted: $body"
