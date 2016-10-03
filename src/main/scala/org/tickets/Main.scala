package org.tickets

import akka.actor.{ActorSystem, Props}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import com.google.inject.Guice
import com.typesafe.config.{Config, ConfigFactory}
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.misc.ActorSlf4j
import org.tickets.module.{AkkaModule, MockUzTokenModule, TelegramModule, UzModule}


object Main extends App with ActorSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val injector = Guice.createInjector(new AkkaModule, new TelegramModule, new UzModule, new MockUzTokenModule)

  log.info(
    """
      | ---------------------------------------------------
      |   Main started in {} ms
      | ---------------------------------------------------
    """.stripMargin, System.currentTimeMillis() - init)
}
