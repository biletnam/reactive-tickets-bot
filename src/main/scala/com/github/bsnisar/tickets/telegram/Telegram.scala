package com.github.bsnisar.tickets.telegram

import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.{Req, Res}
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.telegram.TelegramUpdates.{Reply, Update, Updates}
import com.github.bsnisar.tickets.wire.Wire
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JValue

import scala.concurrent.{ExecutionContext, Future}


trait TelegramPush {


  /**
    * Push message to Telegram.
    * @param reply reply msg
    */
  def push(reply: Reply)(implicit m: Materializer, ex: ExecutionContext): Unit
}

trait TelegramPull {

  /**
    * Pull updates from Telegram.
    * @param offset offset
    * @return updates.
    */
  def pull(offset: Int)(implicit m: Materializer, ex: ExecutionContext): Future[Iterable[Update]]
}

trait Telegram extends TelegramPush with TelegramPull {
}

class TelegramDefault(wire: Wire[Req, JValue]) extends Telegram with LazyLogging with Json {

  override def pull(offset: Int)
                   (implicit m: Materializer, ex: ExecutionContext): Future[Iterable[Update]] = {
    import org.json4s.JValue
    import org.json4s.JsonDSL._

    val reqBody: JValue = "offset" -> offset
    val updatesReq = RequestBuilding.Post("getUpdates", reqBody)
    logger.debug("#pull updates with offset {}", offset)
    val updates: Future[Iterable[Update]] = Source.single(updatesReq -> offset)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[Iterable[Update]])

    updates
  }

  override def push(reply: Reply)(implicit m: Materializer, ex: ExecutionContext): Unit = ???
}
