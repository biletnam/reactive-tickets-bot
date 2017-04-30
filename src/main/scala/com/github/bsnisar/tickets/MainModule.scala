package com.github.bsnisar.tickets

import javax.inject.{Named, Singleton}

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.github.bsnisar.tickets.misc.{StationId, StationIdBase64, Templates}
import com.github.bsnisar.tickets.telegram.TelegramDefault
import com.github.bsnisar.tickets.wire._
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides}
import com.typesafe.config.{Config, ConfigFactory}
import net.codingwell.scalaguice.ScalaModule
import org.json4s.JValue

import scala.concurrent.ExecutionContext


class MainModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    bind[ActorSystem] toInstance system
    bind[Materializer] toInstance materializer
    bind[Config] toInstance ConfigFactory.load()


    bind[StationId].to[StationIdBase64]
    bind[ExecutionContext].annotatedWith(Names.named("default")).toInstance(system.dispatcher)
  }

  @Provides
  @Named("telegram")
  def telegram(@Named("telegramWire") wire: Wire[Ws.Req, JValue], templates: Templates)
              (implicit @Named("default") ex: ExecutionContext, m: Materializer): TelegramDefault = {
    new TelegramDefault(wire, templates)
  }

  @Provides
  @Named("telegramWire")
  @Singleton
  def telegramWire(config: Config)(implicit as: ActorSystem, m: Materializer): Wire[Ws.Req, JValue] = {
    new ProtWire(
      new JsonWire(
        new TgUriWire(
          config.getString("bot.token"),
          new RqWire(config.getString("bot.host"))
        )
      ),
      new TgProtocolBridge
    )
  }

  @Provides
  @Named("uzRailwayWire")
  @Singleton
  def uzWire(config: Config)(implicit as: ActorSystem, m: Materializer): Wire[Ws.Req, JValue] = {
    new ProtWire(
      new JsonWire(
        new RqWire(
          "booking.uz.gov.ua",
          isHttps = false
        )
      ),
      new UzProtocolBridge
    )
  }




}
