package org.tickets.railway.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.scaladsl.Flow

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