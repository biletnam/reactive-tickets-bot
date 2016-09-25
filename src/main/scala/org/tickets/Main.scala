package org.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import org.tickets.misc.Log


object Main extends App with Log {
  val init = System.currentTimeMillis()

  implicit val system = ActorSystem("tickets-bot")
  implicit val materializer = ActorMaterializer()

  log.info("Started in {}", System.currentTimeMillis() - init)
}
