package org.tickets

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.misc.ActorSlf4j


object Main extends App with ActorSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  implicit val system = ActorSystem("tickets-bot")
  implicit val materializer = ActorMaterializer()

  val injector = Guice.createInjector(new AkkaModule)

  log.info("Started in {}", System.currentTimeMillis() - init)
}
