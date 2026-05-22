package com.example

import org.springframework.context.annotation.AnnotationConfigApplicationContext

object SpringCoreIocPoc:

  def main(args: Array[String]): Unit =
    val ctx = AnnotationConfigApplicationContext(classOf[AppConfig])
    try
      val greeter = ctx.getBean(classOf[Greeter])
      println(greeter.greet("Spring IoC"))

      println("\nBeans of type MessageProvider in context:")
      ctx.getBeansOfType(classOf[MessageProvider]).forEach { (name, bean) =>
        println(s"  - $name -> ${bean.getClass.getSimpleName}: \"${bean.message}\"")
      }
    finally ctx.close()
