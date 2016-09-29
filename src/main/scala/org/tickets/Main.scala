package org.tickets

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.typesafe.config.{Config, ConfigFactory}
import org.tickets.bot.tg.Telegram
import org.tickets.misc.Log


object Main extends App with Log {
  val init = System.currentTimeMillis()

  implicit val system = ActorSystem("tickets-bot")
  implicit val materializer = ActorMaterializer()

  val config: Config = ConfigFactory.defaultApplication().resolve()
  log.debug("Config: telegram.url = {}", config.getString("bot.api.host"))
    val flow = Telegram.https(config)

  system.actorOf(Props(classOf[Telegram], flow, materializer))
  log.info("Started in {}", System.currentTimeMillis() - init)
}
