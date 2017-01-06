package com.github.bsnisar.tickets

import akka.http.scaladsl.Http
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Res}


class WireRq(private val url: String) extends Wire[Req, Res] {
  private lazy val poolClientFlow = Http().cachedHostConnectionPool[Ws.Task]("akka.io")

  override def flow: Flow[Req, Res, _] = poolClientFlow
}
