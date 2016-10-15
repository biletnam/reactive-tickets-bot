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

  type Task = Command
  type Req = (HttpRequest, Task)
  type Res = (Try[HttpResponse], Task)

  /**
    * Command for http response.
    */
  trait Command {

    /**
      * Execute command based on http response result.
      *
      * @param response result of response
      * @param mt materializer
      * @param ec executor
      */
    def run(response: Try[HttpResponse])
           (implicit mt: Materializer, ec: ExecutionContext): Unit
  }


  /**
    * Http Pool for UZ API.
    * @return flow for it
    */
  def httpPoolUz(implicit as: ActorSystem, mt: Materializer): Flow[Req, Res, HostConnectionPool] =
    Http().newHostConnectionPool[Task]("booking.uz.gov.ua")




  /**
    * Uz API http flow.
    * @return publishing actor ref
    */
  def uzGraph(implicit as: ActorSystem, mt: Materializer): ActorRef = {
    val publisher = Source.actorRef(1000, OverflowStrategy.dropHead)

    httpPoolUz.to(Sink.actorSubscriber(Props(classOf[CommandConsumer], mt)))
      .runWith(publisher)
  }
}

class CommandConsumer(implicit mt: Materializer) extends ActorSubscriber with LogSlf4j {
  import context.dispatcher

  override protected def requestStrategy: RequestStrategy =
    WatermarkRequestStrategy(1000)

  override def receive: Receive = {
    case OnNext(cmd: Res) =>
      val httpRes = cmd._1
      val command = cmd._2

      command.run(httpRes)

    case a @ _ =>
      log.error("Unhandled {}", a)
  }
}