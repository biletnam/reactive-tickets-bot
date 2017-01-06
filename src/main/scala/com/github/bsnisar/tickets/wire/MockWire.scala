package com.github.bsnisar.tickets.wire

import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req

import org.json4s._
import org.json4s.jackson.JsonMethods._

/**
  * Mock wire that produce predefined JSON.
  * @param json given json as test.
  */
class MockWire(val json: JValue) extends Wire[Req, JValue] {

  def this(jsonStr: String) = {
    this(parse(jsonStr))
  }

  override def flow: Flow[Req, JValue, _] = Flow.fromFunction(
    _ => json
  )
}
