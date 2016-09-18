package org.tickets.api

import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Source}

import scala.util.Try


/**
  * Factory for akk stream for client-server communication as request-reply cycle.
  * @author bsnisar
  */
trait HttpStream {

  /**
    * Http requests source.
    * @return Source.
    */
  def source: Source[(HttpRequest, Int), _]

  /**
    * Request-Reply Flow.
    * @return flow from http request to http reply.
    */
  def flow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _]

  /**
    * Stream that connect source throw http flow.
    * @return Source.Repr viw http client-server flow.
    */
  def stream = source.via(flow)
}