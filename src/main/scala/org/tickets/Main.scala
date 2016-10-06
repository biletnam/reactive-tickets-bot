package org.tickets

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpRequest
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import com.typesafe.config.ConfigFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.bot.{ChatBot, TelegramMethods, TelegramPublisher}
import org.tickets.bot.TelegramMethods.BotToken
import org.tickets.misc.ActorSlf4j
import org.tickets.misc.HttpSupport._


object Main extends App with ActorSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val cfg = ConfigFactory.load().resolve()

  implicit val as = ActorSystem("bot")
  implicit val mt = ActorMaterializer()

  val chatBot = as.actorOf(ChatBot.props)

  val botToken: BotToken = new BotToken(cfg)
  val flow: Flow[Request, Response, _] = TelegramMethods.flow
  TelegramMethods.buildGraph(botToken, flow, chatBot, TelegramPublisher.props)

  log.info(
    """
      | ---------------------------------------------------
      |   Main started in {} ms
      | ---------------------------------------------------
    """.stripMargin, System.currentTimeMillis() - init)
}
