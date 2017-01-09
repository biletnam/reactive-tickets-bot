package com.github.bsnisar.tickets

import com.github.bsnisar.tickets.misc.Json
import org.json4s.{JValue, Reader}

case class ConsUpdate(id: Int, text: String) extends Update

object ConsUpdate {
  implicit object Reader extends Reader[Update] with Json {
    override def read(value: JValue): Update = {
      val id = (value \ "update_id").as[Int]
      val msg = value \ "message"
      val text = msg \ "text"

      ConsUpdate(id, text.as[String])
    }
  }
}

