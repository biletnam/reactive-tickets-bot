package org.tickets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http.HostConnectionPool
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, TypeLiteral}
import com.sandinh.akuice.AkkaGuiceSupport
import com.typesafe.config.{Config, ConfigFactory}
import org.tickets.bot.tg.Telegram
import org.tickets.misc.HttpSupport.{Request, Response}

class AkkaModule(implicit system: ActorSystem, materializer: Materializer) extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[ActorSystem]).toInstance(system)
    bind(classOf[Materializer]).toInstance(materializer)

    val config: Config = ConfigFactory.defaultApplication().resolve()
    bind(classOf[Config]).toInstance(config)

    val telegram = new TypeLiteral[Flow[Request, Response, HostConnectionPool]]() {}
    bind(telegram).annotatedWith(Names.named("telegram-host")).toInstance(Telegram.https(config))
    bindActorFactory

  }
}
