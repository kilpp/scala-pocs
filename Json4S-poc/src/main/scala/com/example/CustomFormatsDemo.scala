package com.example

import org.json4s.*
import org.json4s.native.Serialization.{read, writePretty}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

object CustomFormatsDemo:

  // CustomSerializer[T]: provide (deserialize, serialize) partial functions.
  // The `_` parameter is the active Formats — not needed here.
  class UUIDSerializer
      extends CustomSerializer[UUID](_ =>
        (
          { case JString(s) => UUID.fromString(s) },
          { case u: UUID    => JString(u.toString) }
        )
      )

  class LocalDateSerializer
      extends CustomSerializer[LocalDate](_ =>
        (
          { case JString(s)   => LocalDate.parse(s) },
          { case d: LocalDate => JString(DateTimeFormatter.ISO_LOCAL_DATE.format(d)) }
        )
      )

  // Project a value type to a flat JSON string: "12345.67 USD".
  class MoneySerializer
      extends CustomSerializer[Money](_ =>
        (
          {
            case JString(s) =>
              val Array(amount, ccy) = s.split(" ", 2): @unchecked
              Money((BigDecimal(amount) * 100).toLong, ccy)
          },
          { case Money(cents, ccy) =>
              JString(f"${BigDecimal(cents) / 100}%.2f $ccy")
          }
        )
      )

  // FieldSerializer rewrites field names / drops fields without modifying
  // the case class. Here: rename `owner` ↔ `holder` and drop `opened` on write.
  val accountFieldSerializer: FieldSerializer[Account] = FieldSerializer[Account](
    serializer   = FieldSerializer.renameTo("owner", "holder")
                     .orElse(FieldSerializer.ignore("opened")),
    deserializer = FieldSerializer.renameFrom("holder", "owner")
  )

  given Formats =
    DefaultFormats +
      new UUIDSerializer +
      new LocalDateSerializer +
      new MoneySerializer +
      accountFieldSerializer

  def run(): Unit =
    val account = Account(
      id      = UUID.fromString("00000000-0000-0000-0000-000000000001"),
      owner   = "Ada",
      balance = Money(1234567L, "USD"),
      opened  = LocalDate.of(2026, 5, 9)
    )

    val json = writePretty(account)
    println(json)

    // Reading back: FieldSerializer dropped `opened` on write, so we splice
    // it in to demonstrate the rename direction works.
    val withOpened = json.replace("\n}", ",\n  \"opened\":\"2026-05-09\"\n}")
    val back = read[Account](withOpened)
    println(s"\nback = $back")
