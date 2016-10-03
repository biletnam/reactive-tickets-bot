package org.tickets.module

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.Provider
import com.typesafe.config.Config
import org.tickets.misc.HttpSupport._

class TelegramFlowProvider @Inject() (val cfg: Config,
                                      val actorSystem: ActorSystem,
                                      val materializer: Materializer )
  extends Provider[Flow[Request, Response, Http.HostConnectionPool]] {

  override def get(): Flow[Request, Response, Http.HostConnectionPool] = {
    implicit val mt: Materializer = materializer
    val host: String = cfg.getString("bot.api.host")
    log.info("Connect to telegram host https://{}", host)
    Http(actorSystem).newHostConnectionPoolHttps[Bound](host)
  }
}
