package com.github.bsnisar.tickets.wire

import java.util
import java.util.Collections

import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req
import org.json4s._
import org.json4s.jackson.JsonMethods._

import scala.util.{Failure, Try}

/**
  * Mock wire that produce predefined JSON. Useful for tests.
  *
  * @param json json for test.
  * @author bsnisar
  */
class MockWire(val json: Try[JValue]) extends Wire[Req, JValue] {

  def this(jsonStr: String) = {
    this(Try { parse(jsonStr) })
  }

  def this(ex: Exception) = {
    this(Failure(ex))
  }

  override def flow: Flow[Req, JValue, _] = Flow.fromFunction(
    _ => json.get
  )
}

class GatheringMockWire[A](val origin: Wire[Req, A]) extends Wire[Req, A] {
  val requests: util.List[Req] = Collections.synchronizedList(new util.ArrayList[Req]())

  override def flow: Flow[Req, A, _] = Flow[Req].map(req => {
    requests.add(req); req
  }).via(origin.flow)
}
