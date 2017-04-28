package com.github.bsnisar.tickets

import akka.actor.{ActorContext, ActorRef, ActorSystem}
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.misc.{StationId, StationIdBase64, TemplatesFreemarker}
import com.github.bsnisar.tickets.talk.{Talk, Talks, TgResponses, TgUpdates}
import com.github.bsnisar.tickets.telegram.TelegramDefault
import com.github.bsnisar.tickets.wire._
import com.typesafe.config.ConfigFactory
import org.json4s.JValue

import scala.concurrent.duration._

object Main extends App {
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

  val uzWire: Wire[Ws.Req, JValue] = new ProtWire(
    new JsonWire(
      new RqWire(
        "http://booking.uz.gov.ua",
        isHttps = false
      )
    ),
    new UzProtocolBridge
  )

  val telegram = new TelegramDefault(telegramWire)
  val telegramPush = system.actorOf(TgResponses.props(telegram))
  val stationId: StationId = new StationIdBase64


  val talkFactory = new Talks.BotFactory {
    override def create(name: String, chatID: String)(implicit ac: ActorContext): ActorRef = {
      val props = Talk.props(chatID, stationId, telegramPush)
      ac.actorOf(props, name)
    }
  }

//  val stationsSearcher =
//  val talks = system.actorOf(Talks.props())

}
