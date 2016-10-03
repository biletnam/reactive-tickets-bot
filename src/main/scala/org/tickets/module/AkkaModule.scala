package org.tickets.module

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.AbstractModule
import com.sandinh.akuice.AkkaGuiceSupport
import com.typesafe.config.{Config, ConfigFactory}
import org.tickets.misc.ActorSlf4j

class AkkaModule extends AbstractModule with AkkaGuiceSupport with ActorSlf4j {
  override def configure(): Unit = {
    implicit val system: ActorSystem = ActorSystem("tickets-bot")
    log.info("Initialize ActorSystem[{}]", system)
    bind(classOf[ActorSystem]).toInstance(system)
    val materializer = ActorMaterializer()
    bind(classOf[Materializer]).toInstance(materializer)
    bind(classOf[Config]).toInstance(ConfigFactory.defaultApplication().resolve())
  }
}
