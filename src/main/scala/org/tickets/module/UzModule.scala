package org.tickets.module

import javax.inject.{Inject, Named}

import akka.NotUsed
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.{Flow, Sink, Source}
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

  }

  @Provides
  def token(implicit as: ActorSystem, mt: Materializer): Supplier[String] = {
    implicit val ds = as.dispatcher
    UzToken.singleton
  }

  @Provides @Named("UzTokenFlow")
  def uzTokesFlow(token: Supplier[String]): Flow[Request, Request, _] = {
    UzApi.withTokenFlow(token)
  }

  @Provides @UzRef
  def uzStream(implicit
               @Named("UzAPI") httpFlow: Flow[Request, Response, Http.HostConnectionPool],
               @Named("UzTokenFlow") tokenFlow: Flow[Request, Request, _],
               materializer: Materializer): ActorRef = {

    val publisher: ActorRef = Source.actorRef(100, OverflowStrategy.dropNew)
      .via(tokenFlow)
      .via(httpFlow)
      .runWith(Sink.actorSubscriber(Props(classOf[UzApiSubscriber], materializer)))

    publisher
  }
}



