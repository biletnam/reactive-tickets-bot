package com.github.bsnisar.tickets

import com.github.bsnisar.tickets.misc.Json
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JValue
import org.json4s.JsonAST.{JArray, JObject}

import scala.util.{Failure, Success, Try}

/**
  * Telegram API protocol.
  */
class TgProtocolBridge extends ProtocolBridge with Json with LazyLogging  {
  override def compute(json: JValue): Try[JValue] = {
    val ok = (json \ "ok").as[Boolean]

    if (!ok) {
      val desc = (json \ "description").as[String]
      Failure(new IllegalStateException(s"api error: $desc"))
    } else {
      json \ "result" match {
        case payload @ JArray(_) => Success(payload)
        case payload @ JObject(_) => Success(payload)
        case e @ _ => Failure(new IllegalStateException(s"expect json array, but was ${e.getClass.getSimpleName}"))
      }
    }
  }
}
