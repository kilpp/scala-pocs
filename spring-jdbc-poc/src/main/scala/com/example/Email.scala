package com.example

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.{ReadingConverter, WritingConverter}

// Domain value type. Persisted as VARCHAR via the converters below, registered
// in JdbcConfig. Spring's ConversionService normally short-circuits on null,
// but we guard explicitly to be safe across read/write paths.
final case class Email(value: String):
  override def toString: String = value

object Email:

  @WritingConverter
  final class ToString extends Converter[Email, String]:
    override def convert(source: Email): String =
      if source == null then null else source.value

  @ReadingConverter
  final class FromString extends Converter[String, Email]:
    override def convert(source: String): Email =
      if source == null then null else Email(source)
