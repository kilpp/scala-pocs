package com.example

import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.{User, UserDetailsService}
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler

/** The heart of the POC.
  *
  * `@EnableWebSecurity` turns on Spring Security's filter infrastructure and
  * publishes a `springSecurityFilterChain` bean. We define exactly how that
  * chain behaves with a [[SecurityFilterChain]] bean built from [[HttpSecurity]].
  */
@Configuration
@EnableWebSecurity
class WebSecurityConfig:

  @Bean
  def securityFilterChain(http: HttpSecurity): SecurityFilterChain =
    http
      // ---- authorizeHttpRequests: per-path access rules ----
      .authorizeHttpRequests(auth =>
        auth
          .requestMatchers("/public").permitAll()
          .requestMatchers("/admin/**").hasRole("ADMIN")
          .anyRequest().authenticated()
      )
      // ---- CSRF protection (ON by default; configured explicitly here) ----
      // The default Spring Security 6 handler XOR-masks the token per request
      // (BREACH mitigation). For this header-based non-browser demo we use the
      // plain handler so the raw token from GET /csrf can be echoed back as-is.
      .csrf(csrf =>
        csrf.csrfTokenRequestHandler(CsrfTokenRequestAttributeHandler())
      )
      // HTTP Basic so a script/test can authenticate without a login form.
      // Use our generic 401 writer instead of the default browser challenge.
      .httpBasic(basic => basic.authenticationEntryPoint(authenticationEntryPoint))
      // Replace the container's verbose HTML error pages with a generic JSON
      // body. This 403 handler also catches CSRF rejections (AccessDeniedException).
      .exceptionHandling(ex =>
        ex.authenticationEntryPoint(authenticationEntryPoint)
          .accessDeniedHandler(accessDeniedHandler)
      )
      .build()

  /** 401 — missing/invalid authentication. */
  private val authenticationEntryPoint: AuthenticationEntryPoint =
    (_, response, ex) => writeError(response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage)

  /** 403 — authenticated but not allowed (also fired on CSRF failure). */
  private val accessDeniedHandler: AccessDeniedHandler =
    (_, response, ex) => writeError(response, HttpServletResponse.SC_FORBIDDEN, ex.getMessage)

  /** One generic JSON shape for every security error. */
  private def writeError(response: HttpServletResponse, status: Int, message: String): Unit =
    response.setStatus(status)
    response.setContentType("application/json")
    val safe = Option(message).getOrElse("error").replace("\"", "'")
    response.getWriter.write(s"""{"status":$status,"error":"$safe"}""")

  /** Two in-memory users; `{noop}` means the password is stored as plain text. */
  @Bean
  def userDetailsService(): UserDetailsService =
    val user = User.withUsername("user")
      .password("{noop}password")
      .roles("USER")
      .build()
    val admin = User.withUsername("admin")
      .password("{noop}admin")
      .roles("ADMIN")
      .build()
    InMemoryUserDetailsManager(user, admin)
