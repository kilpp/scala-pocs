package com.example

trait MessageProvider:
  def message: String

class EnglishMessageProvider extends MessageProvider:
  override def message: String = "Hello"

class PortugueseMessageProvider extends MessageProvider:
  override def message: String = "Olá"

class ShoutingMessageProvider(delegate: MessageProvider) extends MessageProvider:
  override def message: String = delegate.message.toUpperCase + "!"
