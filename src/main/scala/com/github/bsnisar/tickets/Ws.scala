package com.github.bsnisar.tickets

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.misc.Json
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s._

import scala.util.Try

object Ws extends Json4sSupport with Json {
  type Task = Any
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)

  type HttpFlow = Flow[Req, Res, Any]

  type WsFlow = Flow[Req, JValue, Any]




  def processProtocol(pb: ProtocolBridge): Flow[JValue, JValue, Any]
    = Flow[JValue].map(json => pb.compute(json).get)

}
