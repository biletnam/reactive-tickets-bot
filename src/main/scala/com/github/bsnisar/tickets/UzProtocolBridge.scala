package com.github.bsnisar.tickets

import com.github.bsnisar.tickets.misc.Json
import org.json4s._

import scala.util.{Failure, Success, Try}

class UzProtocolBridge extends ProtocolBridge with Json {

  override def compute(json: JValue): Try[JValue] = {
    val error = (json \ "error").extract[Boolean]

    if (error) {
      Failure(new IllegalStateException("protocol error"))
    } else {
      json \ "value" match {
        case payload @ JArray(_) => Success(payload)
        case e @ _ => Failure(new IllegalStateException("expect json array"))
      }
    }
  }
}
