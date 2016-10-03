package org.tickets.module

import javax.inject.Named

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import com.google.common.base.Preconditions
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.typesafe.config.Config
import org.tickets.bot.RoutesQuery
import org.tickets.bot.tg.Telegram
import org.tickets.bot.tg.TelegramMethod.BotToken
import org.tickets.misc.ActorSlf4j
import org.tickets.misc.HttpSupport._


class TelegramModule extends AbstractModule with ActorSlf4j {
  override def configure(): Unit = {
    bind(new TypeLiteral[Flow[Request, Response, Http.HostConnectionPool]] {})
      .annotatedWith(Names.named("TelegramFlow"))
      .toProvider(classOf[TelegramFlowProvider])
      .asEagerSingleton()

    bind(classOf[BotToken]).asEagerSingleton()
  }

  @Provides @TelegramProps
  def telegramProps(@Named("TelegramFlow") flow: Flow[Request, Response, Http.HostConnectionPool],
                    materializer: Materializer,
                    botToken: BotToken): Props = {
    Props(classOf[Telegram], flow, materializer, botToken)
  }

  @Provides @Named("Telegram")
  def telegramRef(as: ActorSystem, @TelegramProps props: Props): ActorRef = {
    val ref: ActorRef = as.actorOf(props, "telegram")
    log.info("Initialize telegram actor {}", ref)
    ref
  }

  @Provides @Named("RoutesProps")
  def routesQueryProps(@Named("Telegram") telegramRef: ActorRef,
                       @UzRef uzRef: ActorRef): Props = {
    Props(classOf[RoutesQuery], uzRef, telegramRef)
  }

}


