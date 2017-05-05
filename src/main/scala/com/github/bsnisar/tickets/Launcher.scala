package com.github.bsnisar.tickets

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.misc.{StationId, StationIdBase64, TemplatesFreemarker}
import com.github.bsnisar.tickets.provider.StationsUz
import com.github.bsnisar.tickets.talk._
import com.github.bsnisar.tickets.telegram.TelegramDefault
import com.github.bsnisar.tickets.wire._
import com.typesafe.config.ConfigFactory
import org.json4s.JValue

import scala.concurrent.duration._

object Launcher extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  val config = ConfigFactory.load()

  val telegramWire: Wire[Ws.Req, JValue] = new ProtWire(
    new JsonWire(
      new TgUriWire(
        config.getString("bot.token"),
        new RqWire(config.getString("bot.host"))
      )
    ),
    new TgProtocolBridge
  )

//  val uzWire: Wire[Ws.Req, JValue] = new ProtWire(
//    new JsonWire(
//      new RqWire(
//        "booking.uz.gov.ua",
//        isHttps = false
//      )
//    ),
//    new UzProtocolBridge
//  )

  val uzWire: Wire[Ws.Req, JValue] = new JsonWire(
    new LogWire(
      new RqWire(
        "booking.uz.gov.ua",
        isHttps = false
      )
    )
  )

  val templates = new TemplatesFreemarker()
  val telegram = new TelegramDefault(telegramWire, templates)
  val telegramPushRef = system.actorOf(Answers.props(telegram))


  val uzStations = new StationsUz(uzWire)
  val stationId: StationId = new StationIdBase64
  val stationsTalkRef = system.actorOf(StationsTalk.props(uzStations, stationId, telegramPushRef))

  val routesProps = Routes.props(
    new StationsTalkRoute(stationsTalkRef)
  )

  val routesRef = system.actorOf(routesProps)


  val pullRef = system.actorOf(Updates.props(telegram, routesRef))
  system.scheduler.schedule(1.second, 4.seconds, pullRef, Updates.Tick)


/*
  val talks = system.actorOf(Talks.props(new Talks.BotFactory {
    override def create(name: String, chatID: String)(implicit ac: ActorContext): ActorRef = {
      val props = Talk.props(chatID, stationId, telegramPush)
      ac.actorOf(props, name)
    }
  }))
  val pull = system.actorOf(Updates.props(telegram, talks))

  system.scheduler.schedule(1.second, 4.seconds, pull, Updates.Tick)

*/

}
