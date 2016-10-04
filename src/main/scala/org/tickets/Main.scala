package org.tickets

import com.google.inject.Guice
import org.slf4j.bridge.SLF4JBridgeHandler
import org.tickets.misc.ActorSlf4j


object Main extends App with ActorSlf4j {
  val init = System.currentTimeMillis()
  SLF4JBridgeHandler.removeHandlersForRootLogger()
  SLF4JBridgeHandler.install()

  val injector = Guice.createInjector()

  log.info(
    """
      | ---------------------------------------------------
      |   Main started in {} ms
      | ---------------------------------------------------
    """.stripMargin, System.currentTimeMillis() - init)
}
