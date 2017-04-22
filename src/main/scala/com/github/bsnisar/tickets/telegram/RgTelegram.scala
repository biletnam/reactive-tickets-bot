package com.github.bsnisar.tickets.telegram

import java.util.concurrent.atomic.AtomicInteger

import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire
import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.jackson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

/**
  * Http based telegram API.
  */
class RgTelegram(val wire: Wire[Req, JValue])
                (implicit
                 val mt: Materializer) extends Telegram with Json with LazyLogging {



  override def push(chatId: Long, msg: String): Unit = ???


  override def pull(offset: Int): Future[Iterable[TgUpdate]] = {
    import org.json4s.JValue
    import org.json4s.JsonDSL._

    val reqBody: JValue = "offset" -> offset
    val updatesReq = RequestBuilding.Post("getUpdates", reqBody)
    logger.debug("#pull updates with offset {}", offset)

    import TgUpdate.Reader
    val updates: Future[Iterable[TgUpdate]] = Source.single(updatesReq -> offset)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[Iterable[TgUpdate]])

    updates
  }


  override def info: Future[String] = {
    val getMe = RequestBuilding.Get("/getMe")
    Source.single(getMe -> 42)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => prettyJson(json))
  }
}
