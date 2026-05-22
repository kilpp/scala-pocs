package com.example

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.{Bean, Configuration, Primary, Profile}

@Configuration
class AppConfig:

  @Bean(name = Array("english"))
  @Primary
  def englishProvider(): MessageProvider = EnglishMessageProvider()

  @Bean(name = Array("portuguese"))
  @Profile(Array("pt"))
  def portugueseProvider(): MessageProvider = PortugueseMessageProvider()

  @Bean
  def shouter(@Qualifier("english") base: MessageProvider): MessageProvider =
    ShoutingMessageProvider(base)

  @Bean
  def greeter(provider: MessageProvider): Greeter = Greeter(provider)
