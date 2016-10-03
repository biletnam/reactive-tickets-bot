package org.tickets.module

import javax.inject.Inject

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.inject.Provider
import com.typesafe.config.Config
import org.tickets.bot.uz.UzApi
import org.tickets.misc.HttpSupport._

class UzApiFlowProvider @Inject() (val cfg: Config,
                                   val actorSystem: ActorSystem,
                                   val materializer: Materializer ) extends Provider[Flow[Request, Response, Http.HostConnectionPool]] {

  override def get(): Flow[Request, Response, Http.HostConnectionPool] = {
    implicit val mt: Materializer = materializer
    log.info("Connect to telegram host {}", UzApi.RootPage)
    Http(actorSystem).newHostConnectionPool[Bound](UzApi.RootPageHost)
  }
}
