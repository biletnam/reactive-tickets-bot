package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.talk.StationsSearcher
import com.github.bsnisar.tickets.telegram.RgTelegramUpdates
import com.github.bsnisar.tickets.telegram.actor.TelegramPull
import com.github.bsnisar.tickets.wire._
import com.typesafe.config.ConfigFactory
import org.json4s.JValue

object Main extends App {
  implicit val system = ActorSystem()
  implicit val am = ActorMaterializer()
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

//  val stations: Stations = new StationsUz()
//  val stationsSearch = system.actorOf(StationsSearch.props())

  val updates = new RgTelegramUpdates(wire)
  val puller = system.actorOf(TelegramPull.props(updates, null))

}
