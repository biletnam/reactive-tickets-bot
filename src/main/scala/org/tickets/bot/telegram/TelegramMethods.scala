package org.tickets.bot.telegram

import javax.inject.Inject

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.{ClosedShape, Materializer}
import com.typesafe.config.Config
import org.tickets.misc.HttpSupport.{Bound, Request, Response}
import org.tickets.misc.{LogSlf4j, Req}

/**
  * Created by bsnisar on 07.10.16.
  */
object TelegramMethods extends LogSlf4j {

  /**
    * Https flow to telegram.
    *
    * @param as actor system
    * @param mt materializer
    * @return request to response flow
    */
  def flow(implicit as: ActorSystem, mt: Materializer): Flow[Request, Response, _] = {
    val uri: String = "api.telegram.org"
    log.debug("create Telegram API flow {}", uri)
    Http().newHostConnectionPoolHttps[Bound](uri)
  }

  /**
    * Build graph, connected with remote Telegram API.
    *
    * @param tk telegram bot token
    * @param chatBot root chat bot actor ref
    * @param pushRequests push requests to API
    * @param mt materializer
    * @param as actor system
    */
  def telegramGraph(tk: BotToken, chatBot: ActorRef, pushRequests: Props)
                   (implicit mt: Materializer, as: ActorSystem): Unit = {
    val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      import GraphDSL.Implicits._
      import scala.concurrent.duration._

      val mergeRequests = builder.add(Merge[Request](2))
      val httpFlow = TelegramMethods.flow

      val pushMessagesIn = Source.actorPublisher(pushRequests)
      val request = RequestBuilding.Get(tk.GetUpdatesUri) -> GetUpdatesReq

      val tickPullUpdatesIn = Source.tick(initialDelay = 2.seconds, interval = 25.seconds, request)
      val out = Sink.actorSubscriber(TelegramSubscriber.props(chatBot, mt))

      pushMessagesIn      ~> mergeRequests ~> httpFlow ~> out
      tickPullUpdatesIn   ~> mergeRequests

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
