package com.github.bsnisar.tickets.telegram

import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.telegram.TelegramUpdates.{Update, Updates}
import com.github.bsnisar.tickets.wire.Wire
import com.typesafe.scalalogging.LazyLogging
import org.json4s.{JValue, Reader}

import scala.concurrent.{ExecutionContext, Future}

object TelegramUpdates {
  case class Updates(lastSeq: Int, data: Iterable[Update])

  /**
    * Telegram Update message.
    * @param seqNum single update 'update_id' field
    * @param text update's 'text' field
    * @param chat update's chat id
    */
  final case class Update(seqNum: Int, text: String, chat: String)

  object Update {
    implicit object Reader extends Reader[Update] with Json {
      override def read(value: JValue): Update = {
        val id = (value \ "update_id").as[Int]
        val msg = value \ "message"
        val text = msg \ "text"
        val chatID = "0"

        Update(id, text.as[String], chatID)
      }
    }

    /**
      * Extracting text from update.
      */
    object Text {
      def unapply(arg: Update): Option[String] = Option(arg.text)
    }

  }
}

trait TelegramUpdates {

  /**
    * Pull updates from Telegram.
    * @param offset offset
    * @return updates.
    */
  def pull(offset: Int): Future[Updates]
}


class RgTelegramUpdates(val wire: Wire[Req, JValue])
                       (implicit am: Materializer,
                        ec: ExecutionContext) extends TelegramUpdates with LazyLogging with Json {


  override def pull(offset: Int): Future[Updates] = {
    import org.json4s.JsonDSL._

    val reqBody: JValue = "offset" -> offset
    val updatesReq = RequestBuilding.Post("getUpdates", reqBody)
    logger.debug("#pull updates with offset {}", offset)

    import Update.Reader
    val updates: Future[Iterable[Update]] = Source.single(updatesReq -> offset)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[Iterable[Update]])


    updates.map { data =>
      val lastSeqNum = data.view.map(_.seqNum).max
      Updates(lastSeqNum, data)
    }
  }
}