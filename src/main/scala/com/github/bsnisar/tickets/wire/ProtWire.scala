package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model.HttpRequest
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.ProtocolBridge
import com.github.bsnisar.tickets.Ws.{Req, Task}
import org.json4s.JValue

/**
  * Transform response based on application level protocol,
  * represented in JSON structure.
  *
  * @param origin origin wire
  * @param bridge protocol mapper
  * @author bsnisar
  */
class ProtWire(private val origin: Wire[Req, JValue],
               private val bridge: ProtocolBridge) extends Wire[Req, JValue] {

  override lazy val flow: Flow[(HttpRequest, Task), JValue, _] = {
    Flow[Req].via(origin.flow).map(json => bridge.compute(json).get)
  }
}
