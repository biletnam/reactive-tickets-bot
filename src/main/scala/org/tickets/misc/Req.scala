package org.tickets.misc

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer

import scala.concurrent.ExecutionContext

/**
  * Context of http request.
 *
  * @author bsnisar
  */
trait Req


case object EmptyContext extends Req

/**
  * Envelope of http request that was produced based on message from some actor.
  *
  * @tparam T type of message that was act http request
  */
trait Command[T] extends Req {

  def exec(httpResponse: HttpResponse)(implicit mt: Materializer, ec: ExecutionContext): Unit
}

