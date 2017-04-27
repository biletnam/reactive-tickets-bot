package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.misc.TemplatesFreemarker
import com.github.bsnisar.tickets.talk.{StationsSearcher, UpdatesNotifier}
import com.github.bsnisar.tickets.telegram.{RgTelegramUpdates, TelegramDefault}
import com.github.bsnisar.tickets.telegram.actor.{TelegramPull, TelegramPush}
import com.github.bsnisar.tickets.wire._
import com.typesafe.config.ConfigFactory
import org.json4s.JValue

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

  val telegram = new TelegramDefault(wire)
  val push = system.actorOf(TelegramPush.props(telegram, new TemplatesFreemarker))


  val notifier = system.actorOf(UpdatesNotifier.props(push))
  val puller = system.actorOf(TelegramPull.props(telegram, notifier, materializer))

}
