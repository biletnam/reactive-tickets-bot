package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Res}
import org.json4s.JValue

import scala.util.{Failure, Success}


class LogWire[B](private val origin: Wire[Req, Res]) extends Wire[Req, Res] {
  /**
    * Build flow for transforming input to output.
    *
    * @return flow.
    */
  override def flow: Flow[Req, Res, _] = Flow.fromFunction[Req, Res] { r =>

    ???
  }
}
