package com.example

import java.net.{CookieManager, URI}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.file.Files
import java.util.Base64

import org.apache.catalina.core.StandardContext
import org.apache.catalina.startup.Tomcat
import org.apache.tomcat.util.descriptor.web.{FilterDef, FilterMap}
import org.springframework.web.context.ContextLoaderListener
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext
import org.springframework.web.filter.DelegatingFilterProxy
import org.springframework.web.servlet.DispatcherServlet

/** Boots the secured app on embedded Tomcat and fires real HTTP requests to
  * show the [[SecurityFilterChain]] rules and CSRF protection in action.
  */
object SpringSecurityPoc:

  private val port = 8080
  private val base = s"http://localhost:$port"

  def main(args: Array[String]): Unit =
    val tomcat = startTomcat()
    try
      // A cookie-aware client so JSESSIONID (hence the CSRF token) survives
      // across requests, just like a browser would keep it.
      val http = HttpClient.newBuilder().cookieHandler(CookieManager()).build()

      println("=== authorizeHttpRequests ===")
      show(http, "GET /public          (permitAll)        ", get("/public"))
      show(http, "GET /private         (no credentials)   ", get("/private"))
      show(http, "GET /private         (user:password)    ", get("/private", basic("user", "password")))
      show(http, "GET /admin/info      (user → forbidden) ", get("/admin/info", basic("user", "password")))
      show(http, "GET /admin/info      (admin → allowed)  ", get("/admin/info", basic("admin", "admin")))

      println("\n=== CSRF protection ===")
      show(http, "POST /messages       (no CSRF token)    ", post("/messages", "hello", basic("user", "password")))

      // Fetch the session's CSRF token, then replay it on the next POST.
      val token = http.send(get("/csrf", basic("user", "password")), HttpResponse.BodyHandlers.ofString()).body()
      println(s"\nfetched CSRF token from GET /csrf -> $token")
      val withToken = post("/messages", "hello", basic("user", "password"), "X-CSRF-TOKEN" -> token)
      show(http, "POST /messages       (with CSRF token)  ", withToken)
    finally
      tomcat.stop()
      tomcat.destroy()

  // --- request builders -------------------------------------------------

  private def get(path: String, headers: (String, String)*): HttpRequest =
    var b = HttpRequest.newBuilder(URI.create(base + path)).GET()
    headers.foreach((k, v) => b = b.header(k, v))
    b.build()

  private def post(path: String, body: String, headers: (String, String)*): HttpRequest =
    var b = HttpRequest.newBuilder(URI.create(base + path))
      .header("Content-Type", "text/plain")
      .POST(HttpRequest.BodyPublishers.ofString(body))
    headers.foreach((k, v) => b = b.header(k, v))
    b.build()

  private def basic(user: String, pass: String): (String, String) =
    val encoded = Base64.getEncoder.encodeToString(s"$user:$pass".getBytes)
    "Authorization" -> s"Basic $encoded"

  private def show(http: HttpClient, label: String, request: HttpRequest): Unit =
    val resp = http.send(request, HttpResponse.BodyHandlers.ofString())
    // Bodies are one-liners now: plain text on success, generic JSON on error.
    val detail = resp.body().linesIterator.nextOption().getOrElse("")
    println(f"  $label -> ${resp.statusCode()}%3d  $detail")

  // --- embedded Tomcat + Spring wiring ----------------------------------

  private def startTomcat(): Tomcat =
    val appContext = AnnotationConfigWebApplicationContext()
    appContext.register(classOf[WebConfig], classOf[WebSecurityConfig])

    val baseDir = Files.createTempDirectory("tomcat-poc").toFile
    val tomcat = Tomcat()
    tomcat.setBaseDir(baseDir.getAbsolutePath)
    tomcat.setPort(port)
    tomcat.getConnector() // force the default connector to be created

    val ctx = tomcat.addContext("", baseDir.getAbsolutePath).asInstanceOf[StandardContext]

    // ContextLoaderListener publishes appContext as the *root* WebApplicationContext.
    // It runs at startup with the ServletContext in hand, so the @EnableWebMvc /
    // @EnableWebSecurity beans refresh correctly — and it does so before the
    // security filter initializes (which is why we don't share the context manually).
    ctx.addApplicationLifecycleListener(ContextLoaderListener(appContext))

    // DispatcherServlet reuses the already-active root context to dispatch to handlers.
    val dispatcher = DispatcherServlet(appContext)
    val servlet = Tomcat.addServlet(ctx, "dispatcher", dispatcher)
    servlet.setLoadOnStartup(1)
    ctx.addServletMappingDecoded("/", "dispatcher")

    // DelegatingFilterProxy hands every request to Spring Security's
    // springSecurityFilterChain bean, found in the root context by name.
    val securityFilter = DelegatingFilterProxy("springSecurityFilterChain")
    val filterDef = FilterDef()
    filterDef.setFilterName("springSecurityFilterChain")
    filterDef.setFilter(securityFilter)
    ctx.addFilterDef(filterDef)
    val filterMap = FilterMap()
    filterMap.setFilterName("springSecurityFilterChain")
    filterMap.addURLPattern("/*")
    ctx.addFilterMap(filterMap)

    tomcat.start()
    println(s"Embedded Tomcat started on $base\n")
    tomcat
