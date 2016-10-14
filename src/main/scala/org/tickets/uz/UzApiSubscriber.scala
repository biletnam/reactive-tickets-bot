package org.tickets.uz

import akka.actor.Props
import akka.http.scaladsl.model.HttpResponse
import akka.stream.Materializer
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import org.tickets.misc.{LogSlf4j, Req}
import org.tickets.uz.cmd.Command

import scala.util.{Failure, Success}

object UzApiSubscriber {

  def props(mt: Materializer): Props = {
    Props(classOf[UzApiSubscriber], mt)
  }
}

/**
  * Subscriber for http requests to UZ API.
  */
class UzApiSubscriber(implicit mt: Materializer) extends ActorSubscriber with LogSlf4j {
  import context.dispatcher

  override
  protected def requestStrategy: RequestStrategy = WatermarkRequestStrategy(10)

  override def receive: Receive = {
    case OnNext((Success(httpResp: HttpResponse), boundContext: Req)) =>
      onSuccessMsg(httpResp, boundContext)
    case a @ _ => log.error("Unhandled {}", a)
  }

  private def onSuccessMsg(resp: HttpResponse, req: Req) = req match {
    case ev: Command => ev.exec(resp)
  }
}
