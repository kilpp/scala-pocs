package com.example

class Greeter(provider: MessageProvider):
  def greet(name: String): String = s"${provider.message}, $name"
