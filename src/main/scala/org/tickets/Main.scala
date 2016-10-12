package org.tickets

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.BotToken
import org.tickets.telegram.{Pull, Push, Telegram}

object Main extends App with LogSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val cfg = ConfigFactory.load().resolve()
  val token = BotToken(cfg.getString("bot.api.token"))

  implicit val system = ActorSystem("bot")
  implicit val mt = ActorMaterializer()

  val httpFlow = Telegram.httpFlow
  val dest = system.actorOf(Props[Echo])
  val pullRef = system.actorOf(Pull.props(httpFlow, token, dest))
  val pushRef = system.actorOf(Push.props(httpFlow, token))

  system.scheduler.schedule(initialDelay = 1.second,
    interval = 15.seconds,
    receiver = pullRef,
    message = Pull.Tick)(system.dispatcher)

  log.info(
    """
      | ---------------------------------------------------
      |   Main started in {} ms
      | ---------------------------------------------------
    """.stripMargin, System.currentTimeMillis() - init)
}
