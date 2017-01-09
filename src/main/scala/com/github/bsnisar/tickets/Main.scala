package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.wire._
import com.typesafe.config.ConfigFactory
import org.json4s.JValue

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App {
 val config = ConfigFactory.load()
 implicit val actorSystem = ActorSystem()
 implicit val materializer = ActorMaterializer()

  val wire: Wire[Req, JValue] = new ProtWire(
    new JsonWire(
      new TgUriWire(
        config.getString("bot.token"),
        new RqWire(
          config.getString("bot.host")
        )
      )
    ),
    new TgProtocolBridge
  )

  val telegram: Telegram = new RgTelegram(wire)
  val updates: Updates = telegram.updates

  updates.pull.foreach(println(_))

  telegram.info.foreach(println(_))
}
