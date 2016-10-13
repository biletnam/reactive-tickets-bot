package org.tickets

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.bot.Talks
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.HttpFlow
import org.tickets.telegram.{MethodBindings, Pull, Push, Telegram}

import scala.concurrent.duration._

object Main extends App with LogSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val cfg = ConfigFactory.load().resolve()
  val token = MethodBindings(cfg.getString("bot.api.token"))

  implicit val system = ActorSystem("bot")
  implicit val mt = ActorMaterializer()

  val httpFlow: HttpFlow = Telegram.httpFlow
  val pushRef:  ActorRef = system.actorOf(Push.props(httpFlow, token))
  val dest:     ActorRef = system.actorOf(Talks.props(pushRef))
  val pullRef:  ActorRef = system.actorOf(Pull.props(httpFlow, token, dest))

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
