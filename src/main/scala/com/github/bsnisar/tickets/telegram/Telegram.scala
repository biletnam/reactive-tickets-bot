package com.github.bsnisar.tickets.telegram

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.{Json, Templates}
import com.github.bsnisar.tickets.talk.ResponsesSender.Reply
import com.github.bsnisar.tickets.telegram.UpdatesSource.UpdatesEvent
import com.github.bsnisar.tickets.wire.Wire
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JValue

import scala.concurrent.{ExecutionContext, Future}


trait TelegramPush {


  /**
    * Push message to Telegram.
    * @param reply reply msg
    */
  def push(reply: Reply): Unit
}

object UpdatesSource {
  case class UpdatesEvent(updates: Iterable[Update])
}

trait UpdatesSource {

  /**
    * Pull updates from Telegram.
    * @param offset offset
    * @return updates.
    */
  def pull(offset: Int): Future[UpdatesEvent]
}


class TelegramDefault(wire: Wire[Req, JValue], val templates: Templates)
                     (implicit m: Materializer, ex: ExecutionContext)
  extends TelegramPush with UpdatesSource with LazyLogging with Json {

  override def pull(offset: Int): Future[UpdatesEvent] = {

    val uri = Uri("getUpdates").withQuery(Query("offset" -> String.valueOf(offset)))
    val updatesReq = RequestBuilding.Get(uri)
    logger.debug("#pull updates with offset {}", offset)
    val updates = Source.single(updatesReq -> offset)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => UpdatesEvent(updates = json.as[Iterable[Update]]))

    updates
  }

  override def push(reply: Reply): Unit = {
    import org.json4s.JsonDSL._

    val chat = reply.chatID
    val payload = templates.renderMsg(reply.msg)
    val json =
        ("chat_id" -> chat) ~
        ("text" -> payload)

    val req: HttpRequest = RequestBuilding.Post("/sendMessage", json)
    Source.single(req -> 1)
      .via(wire.flow)
      .runWith(Sink.head)
  }
}
