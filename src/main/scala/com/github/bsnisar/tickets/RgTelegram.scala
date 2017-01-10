package com.github.bsnisar.tickets
import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire

import org.json4s._
import org.json4s.jackson._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Http based telegram API.
  */
class RgTelegram(val wire: Wire[Req, JValue])
                (implicit
                 val mt: Materializer) extends Telegram with Json {

  override def push(chatId: Long, msg: String): Unit = ???

  override val updates: Updates = new TgUpdates(wire)

  override def info: Future[String] = {
    val getMe = RequestBuilding.Get("/getMe")
    Source.single(getMe -> 42)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => prettyJson(json))
  }
}
