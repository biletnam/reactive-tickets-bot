package org.tickets.module

import javax.inject.{Inject, Named}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.javadsl.{Sink, Source}
import akka.stream.scaladsl.Flow
import com.google.common.base.{Supplier, Suppliers}
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provider, Provides, TypeLiteral}
import com.typesafe.config.Config
import org.tickets.bot.uz.{UzApi, UzApiSubscriber, UzToken}
import org.tickets.misc.HttpSupport._


class UzModule extends AbstractModule {

  override def configure(): Unit = {

    bind(new TypeLiteral[Flow[Request, Response, Http.HostConnectionPool]](){})
      .annotatedWith(Names.named("UzAPI"))
      .toProvider(classOf[UzApiFlowProvider])
      .asEagerSingleton()

    bind(new TypeLiteral[Flow[Request, Request, _]]() {} )
      .annotatedWith(Names.named("UzTokenFlow"))
      .toInstance(UzApi.withTokenFlow(Suppliers.ofInstance("test")))
  }

  @Provides @Named("UzApiStream")
  def uzStream(@Named("UzAPI") httpFlow: Flow[Request, Response, Http.HostConnectionPool],
               @Named("UzTokenFlow") tokenFlow: Flow[Request, Request, _],
               materializer: Materializer): ActorRef = {

    val flow = Flow[Request]
      .via(tokenFlow)
      .via(httpFlow)

    val publisher: ActorRef = Source.actorRef(100, OverflowStrategy.dropNew)
      .via(flow)
      .runWith(Sink.actorSubscriber(Props[UzApiSubscriber]), materializer)

    publisher
  }
}


class UzApiFlowProvider @Inject() (val cfg: Config,
                                   val actorSystem: ActorSystem,
                                   val materializer: Materializer ) extends Provider[Flow[Request, Response, Http.HostConnectionPool]] {

  override def get(): Flow[Request, Response, Http.HostConnectionPool] = {
    implicit val mt: Materializer = materializer
    log.info("Connect to telegram host {}", UzApi.RootPage)
    Http(actorSystem).newHostConnectionPool[Bound](UzApi.RootPageHost)
  }
}
