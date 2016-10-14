package org.tickets

import akka.actor.{ActorRef, ActorSystem}
import akka.dispatch.MessageDispatcher
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.bot.Talks
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Telegram.HttpFlow
import org.tickets.telegram.{MethodBindings, TelegramPull, TelegramPush, Telegram}

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
  val pushRef: ActorRef = system.actorOf(TelegramPush.props(httpFlow, token))
  val dest: ActorRef = system.actorOf(Talks.props(pushRef))
  val pullRef: ActorRef = system.actorOf(TelegramPull.props(httpFlow, token, dest))
//  val railwayPull:  ActorRef = system.actorOf(RailwayRoutesPull.props)

  // setup periodic ticks
  system.scheduler.schedule(initialDelay = 1.second,
    interval = 15.seconds,
    receiver = pullRef,
    message = TelegramPull.Tick)(system.dispatcher)

  /*system.scheduler.schedule(initialDelay = 1.second,
    interval = 30.seconds,
    receiver = railwayPull,
    message = RailwayRoutesPull.PullNext)(system.dispatcher)*/


  log.info(
    """
      | ---------------------------------------------------
      |   Main started in {} ms
      | ---------------------------------------------------
    """.stripMargin, System.currentTimeMillis() - init)
}
