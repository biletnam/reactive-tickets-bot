package org.tickets.api

import javax.inject.Named

import akka.actor.{Actor, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.{ActorMaterializer, Materializer}
import com.google.inject.name.Names
import com.google.inject.{AbstractModule, Provides}
import net.codingwell.scalaguice.ScalaModule

import scala.util.Try

/**
  * Created by bsnisar on 16.09.16.
  */
class ApiModule(val sys: ActorSystem) extends AbstractModule with ScalaModule {
  private implicit val _system = sys

  override def configure(): Unit = {
    bind[Actor].annotatedWith(Names.named(Telegram.name)).to[Telegram]
  }

  @Provides
  def materializer(): Materializer = {
    ActorMaterializer()
  }

  @Provides
  def httpsFlow(@Named("telegram.host") host: String,
                materializer: Materializer): HttpStream = {
    implicit val mz = materializer
    val httpFlow = Http().newHostConnectionPoolHttps[Int](host = host)

    new HttpStream {
      override def flow: Flow[(HttpRequest, Int), (Try[HttpResponse], Int), _] = httpFlow
      override def source: Source[(HttpRequest, Int), _] = ???
    }
  }

}
