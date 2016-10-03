package org.tickets.module

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provider}
import com.typesafe.config.Config
import org.tickets.bot.uz.UzApi
import org.tickets.misc.ActorSlf4j
import org.tickets.misc.HttpSupport._


class TelegramModule extends AbstractModule with ActorSlf4j {
  override def configure(): Unit = {
    bind(classOf[Flow[Request, Response, Http.HostConnectionPool]])
      .annotatedWith(Names.named("Telegram"))
      .toProvider(classOf[TelegramFlowProvider])
      .asEagerSingleton()

  }
}

class TelegramFlowProvider @Inject() (val cfg: Config,
                                      val actorSystem: ActorSystem,
                                      val materializer: Materializer ) extends Provider[Flow[Request, Response, Http.HostConnectionPool]] {

  override def get(): Flow[Request, Response, Http.HostConnectionPool] = {
    implicit val mt: Materializer = materializer
    val host: String = cfg.getString("bot.api.host")
    log.info("Connect to telegram host https://{}", host)
    Http(actorSystem).newHostConnectionPoolHttps[Bound](host)
  }
}
