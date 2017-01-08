package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Res, Task}

import scala.util.Try

/**
  * Created by bsnisar on 08.01.17.
  */
class UriWire extends Wire[Req, Req]{
  override def flow: Flow[(HttpRequest, Task), Req, _] =
    Flow[Req].map {
      case req @ (rq, _) =>
        rq.getUri().pathSegments()
        req
    }
}
