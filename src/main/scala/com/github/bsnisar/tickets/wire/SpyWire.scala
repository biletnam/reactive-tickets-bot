package com.github.bsnisar.tickets.wire

import java.util
import java.util.Collections
import java.util.concurrent.CountDownLatch

import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req

import scala.concurrent.duration.Duration

/**
  * Wire for tests. Cache all requests.
  */
class SpyWire[A](val origin: Wire[Req, A]) extends Wire[Req, A]  {
  val requests: util.List[Req] = Collections.synchronizedList(new util.ArrayList[Req]())
  private val cdl = new CountDownLatch(1)

  def awaitTransmission(d: Duration): Unit = {
    require(cdl.await(d._1, d._2), "no messages were transmitted")
  }

  override def flow: Flow[Req, A, _] = Flow[Req].map(req => {
    requests.add(req); cdl.countDown(); req
  }).via(origin.flow)
}
