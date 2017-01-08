package com.github.bsnisar.tickets.wire

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Res}
import com.github.bsnisar.tickets.Ws


/**
  * Wire with http connection to specified host.
  * @param url host url
  */
class RqWire(private val url: String)(private implicit val as: ActorSystem,
                                      private implicit val mt: Materializer) extends Wire[Req, Res] {

  private lazy val poolClientFlow = Http().cachedHostConnectionPoolHttps[Ws.Task](url)

  override def flow: Flow[Req, Res, _] = poolClientFlow
}
