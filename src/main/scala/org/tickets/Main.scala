package org.tickets

import akka.actor.{ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.bot.DefineRouteTalk.TalkProps
import org.tickets.bot.Talks
import org.tickets.misc.LogSlf4j
import org.tickets.railway.uz.UzApiRailwayStations
import org.tickets.railway.{RailwayApi, RailwayStations}
import org.tickets.telegram.TelegramApi.HttpFlow
import org.tickets.telegram.{MethodBindings, TelegramApi, TelegramPull, TelegramPush}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object Main extends App with LogSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val cfg = ConfigFactory.load().resolve()
  implicit val system = ActorSystem("bot")
  implicit val mt = ActorMaterializer()

  val telegramMethods = MethodBindings(cfg.getString("bot.api.token"))
  implicit val defaultContext: ExecutionContext = system.dispatcher

  val httpFlow: HttpFlow = TelegramApi.httpFlow
  val stations: RailwayStations = new UzApiRailwayStations(RailwayApi.httpFlowUzApi)

  val pushRef: ActorRef = system.actorOf(TelegramPush.props(httpFlow, telegramMethods), "telegram_push")
  val dest: ActorRef = system.actorOf(Talks.props(new TalkProps(stations, pushRef)), "talks")
  val pullRef: ActorRef = system.actorOf(TelegramPull.props(httpFlow, telegramMethods, dest), "telegram_pull")

  // periodic telegram update pulls
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
