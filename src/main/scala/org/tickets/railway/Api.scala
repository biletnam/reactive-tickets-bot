package org.tickets.railway

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.actor.ActorSubscriberMessage.OnNext
import akka.stream.actor.{ActorSubscriber, RequestStrategy, WatermarkRequestStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
import org.tickets.misc.LogSlf4j
import org.tickets.railway.Api.Res

import scala.concurrent.ExecutionContext
import scala.util.Try

object Api {

  type Task = Int
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)

  type ApiFlow = Flow[Req, Res, Any]

  /**
    * Http Pool for UZ API.
    * @return flow for it
    */
  def httpFlowUzApi(implicit as: ActorSystem, mt: Materializer): ApiFlow = {
    Http().newHostConnectionPool[Task]("booking.uz.gov.ua")
  }
}