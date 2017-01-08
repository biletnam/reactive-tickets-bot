package com.github.bsnisar.tickets
import java.util.concurrent.ForkJoinPool

import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire
import org.json4s._
import org.json4s.JsonDSL._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Http based telegram API.
  */
class RgTelegram(val wire: Wire[Req, JValue])
                (implicit
                 val mt: Materializer) extends Telegram with Json {

  override def push(chatId: Long, msg: String): Unit = ???

  override def pull(offset: Int): Future[Iterable[Any]] = {
    val reqBody: JValue = "offset" -> offset
    val updatesReq = RequestBuilding.Post("getUpdates", reqBody)

    import ConsUpdate.Reader

    val updates: Future[Iterable[Update]] = Source.single(updatesReq -> 1)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[Iterable[Update]])

    updates
  }
}
