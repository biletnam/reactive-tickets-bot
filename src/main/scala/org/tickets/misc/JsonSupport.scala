package org.tickets.misc

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import org.json4s.JsonAST.{JString, JValue}
import org.json4s.jackson.Serialization
import org.json4s.{CustomSerializer, JValue, NoTypeHints, Reader, Serialization, Writer}

object JsonSupport {

  private class LocalDateSerializer extends CustomSerializer[LocalDate](format => (
    {
      case JString(time) => LocalDate.parse(time, DateTimeFormatter.ISO_DATE)
    },
    {
      case obj: LocalDate => JString(obj.format(DateTimeFormatter.ISO_DATE))
    }
    ))

  implicit def serialization: Serialization = org.json4s.jackson.Serialization

  implicit val formats = Serialization.formats(NoTypeHints) + new LocalDateSerializer

}
