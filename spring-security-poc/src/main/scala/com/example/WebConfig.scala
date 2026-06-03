package com.example

import org.springframework.context.annotation.{Bean, Configuration}
import org.springframework.web.servlet.config.annotation.EnableWebMvc

/** Minimal Spring MVC setup: `@EnableWebMvc` wires the DispatcherServlet
  * machinery (handler mapping + the built-in String/JSON message converters)
  * and we register the single controller as a bean.
  */
@Configuration
@EnableWebMvc
class WebConfig:

  @Bean
  def apiController(): ApiController = ApiController()
