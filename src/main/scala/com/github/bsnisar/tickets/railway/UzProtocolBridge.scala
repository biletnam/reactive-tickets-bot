package com.github.bsnisar.tickets.railway

import com.github.bsnisar.tickets.ProtocolBridge
import com.github.bsnisar.tickets.misc.ApiProtocolException
import org.json4s._

import scala.util.{Failure, Success, Try}

class UzProtocolBridge extends ProtocolBridge {

  override def compute(json: JValue): Try[JValue] = {
    val error = (json \ "error").extract[Boolean]

    if (error) {
      Failure(new ApiProtocolException(""))
    } else {
      val result = json \ "value" match {
        case payload @ JArray(_) => Success(payload)
        case e @ _ => Failure(new ApiProtocolException(""))
      }

      result
    }
  }
}
