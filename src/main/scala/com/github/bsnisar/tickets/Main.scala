package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory

object Main extends App {
 val config = ConfigFactory.load()
 implicit val actorSystem = ActorSystem()
 implicit val materializer = ActorMaterializer()
 val botUrl = s"${config.getString("bot.host")}}"

}
