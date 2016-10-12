package org.tickets.misc

import org.json4s.Serialization

object JsonUtil {
  implicit def serialization: Serialization = org.json4s.jackson.Serialization
  implicit val formats = org.json4s.DefaultFormats
}
