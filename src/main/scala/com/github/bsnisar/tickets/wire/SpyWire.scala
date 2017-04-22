package com.github.bsnisar.tickets.wire

import java.util
import java.util.Collections

import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req

/**
  * Wire for tests. Cache all requests.
  */
class SpyWire[A](val origin: Wire[Req, A]) extends Wire[Req, A]  {
  val requests: util.List[Req] = Collections.synchronizedList(new util.ArrayList[Req]())

  override def flow: Flow[Req, A, _] = Flow[Req].map(req => {
    requests.add(req); req
  }).via(origin.flow)
}
