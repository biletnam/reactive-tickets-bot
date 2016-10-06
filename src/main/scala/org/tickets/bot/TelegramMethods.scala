package org.tickets.bot

import javax.inject.Inject

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri
import akka.stream.{ClosedShape, Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import com.typesafe.config.Config
import org.tickets.bot.tg.TelegramMethod._
import org.tickets.misc.HttpSupport.{Bound, Request, Response}
import org.tickets.misc.Req

/**
  * Created by bsnisar on 07.10.16.
  */
object TelegramMethods {

  def flow(implicit as: ActorSystem, mt: Materializer): Flow[Request, Response, _] = {
    Http().newHostConnectionPoolHttps[Bound]("api.telegram.org")
  }

  def buildGraph(tk: BotToken, httpFlow: Flow[Request, Response, _], chatBot: ActorRef, publisher: Props)
                (implicit mt: Materializer): Unit = {
    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      import scala.concurrent.duration._
      val request = RequestBuilding.Get(tk.GetUpdatesUri) -> GetUpdatesReq

      val publishingIn = Source.actorPublisher(publisher)
      val scheduleIn = Source.tick(initialDelay = 2.seconds, interval = 25.seconds, request)
      val out = Sink.actorSubscriber(TelegramSubscriber.props(chatBot, mt))

      val requestsMerge = builder.add(Merge[Request](2))

      publishingIn  ~> requestsMerge ~> httpFlow ~> out
      scheduleIn    ~> requestsMerge

      ClosedShape
    })

    graph.run()
  }

  case object GetUpdatesReq extends Req

  /**
    * TelegramRef method bot token.
    * @param value token value
    */
  case class BotToken(value: String) {
    log.info("Bot token -- {}", value)

    @Inject()
    def this(cfg: Config) = {
      this(cfg.getString("bot.api.token"))
    }

    lazy val GetUpdatesUri: Uri = Uri(s"/bot$value/getUpdates")
    lazy val SendMessageUri: Uri = Uri(s"/bot$value/sendMessage")
  }
}
