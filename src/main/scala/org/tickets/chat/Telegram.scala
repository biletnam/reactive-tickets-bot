package org.tickets.chat

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream._
import akka.stream.scaladsl.{Balance, Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import org.tickets.misc.LogSlf4j

import scala.util.{Failure, Success, Try}

object Telegram  extends LogSlf4j {
  type TgMethod = Method
  type TgReq = (HttpRequest, TgMethod)
  type TgRes = (Try[HttpResponse], TgMethod)

  /**
    * TelegramRef method bot token.
    * @param value token value
    */
  case class BotToken(value: String) {
    log.info("Bot token -- {}", value)

    lazy val GetUpdatesUri: Uri = Uri(s"/bot$value/getUpdates")
    lazy val SendMessageUri: Uri = Uri(s"/bot$value/sendMessage")
  }

  /**
    * Https flow to telegram.
    *
    * @param as actor system
    * @param mt materializer
    * @return request to response flow
    */
  def flow(implicit as: ActorSystem, mt: Materializer): Flow[TgReq, TgRes, _] = {
    val uri: String = "api.telegram.org"
    log.debug("create Telegram API flow {}", uri)
    Http().newHostConnectionPoolHttps[TgMethod](uri)
  }

  def telegramGraph(tk: BotToken, chatBot: ActorRef, pushRequests: Props)
                   (implicit mt: Materializer, as: ActorSystem): Unit = {
    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      import scala.concurrent.duration._


      val tickSrc:    SourceShape[Pull.Tick]          = builder.add(Source.tick(initialDelay = 2.seconds, interval = 15.seconds, tick = Pull.Tick))
      val connection: FlowShape[TgReq, TgRes]         = builder.add(flow)
      val pullBracer: FlowShape[Pull.Tick, Pull.Pull] = builder.add(new CursorPuller)



      ClosedShape
    })

    graph.run()
  }


}

