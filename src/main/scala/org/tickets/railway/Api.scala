package org.tickets.railway

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream._
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import org.tickets.misc.HttpSupport._
import org.tickets.uz.UzApiSubscriber

import scala.concurrent.ExecutionContext
import scala.util.Try

object Api {

  type Task = Command
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)

  trait Command {
    def run(response: Try[HttpResponse])
           (implicit mt: Materializer, ec: ExecutionContext): Unit
  }


  /**
    * Http Pool for UZ API.
    * @return flow for it
    */
  def httpPoolUz(implicit as: ActorSystem, mt: Materializer): Flow[Req, Res, HostConnectionPool] =
    Http().newHostConnectionPool[Task]("booking.uz.gov.ua")


  def uzGraph(implicit as: ActorSystem, mt: Materializer): ActorRef = {
    val publisher = Source.actorRef(100, OverflowStrategy.dropHead)

    httpPoolUz.to(Sink.actorSubscriber(UzApiSubscriber.props(mt)))
      .runWith(publisher)
  }

}
