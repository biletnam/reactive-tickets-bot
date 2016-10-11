package org.tickets.chat

import akka.http.scaladsl.model.HttpResponse
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}

import scala.util.{Failure, Success}

class ConsumeUpdates extends ActorSubscriber {
  override protected def requestStrategy: RequestStrategy =
    WatermarkRequestStrategy(100)

  override def receive: Receive = {
    case OnNext((Success(httpResp: HttpResponse), Telegram.Updates)) =>
      ???
    case OnNext((Failure(error), Telegram.Updates)) =>
      ???
  }
}
