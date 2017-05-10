package com.github.bsnisar.tickets.wire

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws
import com.github.bsnisar.tickets.Ws.{Req, Res}


/**
  * Wire with HTTPS connection to specified host. Backed by Akka Http connection pool.
  *
  * @param url host url
  * @author bsnisar
  */
class RqWire(private val url: String, isHttps: Boolean = true)
            (implicit val as: ActorSystem, val mt: Materializer) extends Wire[Req, Res] {


  private lazy val poolClientFlow = if (isHttps) {
    Http().cachedHostConnectionPoolHttps[Ws.Task](url)
  } else {
    Http().cachedHostConnectionPool[Ws.Task](url)
  }

  override def flow: Flow[Req, Res, _] = poolClientFlow
}
