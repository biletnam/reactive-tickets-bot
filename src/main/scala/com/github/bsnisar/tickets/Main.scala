package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.misc.TemplatesFreemarker
import com.github.bsnisar.tickets.talk.{Talks, TgUpdates}
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

  val wire: Wire[Ws.Req, JValue] = new ProtWire(
    new JsonWire(
      new TgUriWire(
        config.getString("bot.token"),
        new RqWire(config.getString("bot.host"))
      )
    ),
    new TgProtocolBridge
  )

//  val stationsSearcher =
//  val talks = system.actorOf(Talks.props())
  
}
