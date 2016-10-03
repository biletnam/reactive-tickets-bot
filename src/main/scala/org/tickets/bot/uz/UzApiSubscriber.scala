package org.tickets.bot.uz

import akka.actor.Props
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import org.tickets.misc.{Command, ActorSlf4j, Req}

import scala.util.{Failure, Success}

object UzApiSubscriber {

  def props(mt: Materializer): Props = {
    Props(classOf[UzApiSubscriber], mt)
  }
}

/**
  * Subscriber for http requests to UZ API.
  */
class UzApiSubscriber(implicit mt: Materializer) extends ActorSubscriber with ActorSlf4j {
  import context.dispatcher

  override
  protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(50)

  override def receive: Receive = {
    case (Success(httpResp: HttpResponse), boundContext: Req) =>
      log.info("Consume message {}", boundContext)
      onSuccessMsg(httpResp, boundContext)
    case (Failure(error), ctx) =>
      log.error("Http request failed {}", ctx, error)
    case m @ _ => log.warn("Unhandled {}", m)
  }

  private def onSuccessMsg(resp: HttpResponse, req: Req) = req match {
    case ev: Command[_] => ev.exec(resp)
  }
}
