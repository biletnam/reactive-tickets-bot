package org.tickets.telegram

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.stream._
import akka.stream.scaladsl.Flow
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Method.{TgMethod, TgReq, TgRes}

object Telegram  extends LogSlf4j {

  type HttpFlow = Flow[TgReq, TgRes, Any]

  /**
    * TelegramRef method bot token.
    * @param value token value
    */
  case class BotToken(value: String) {
    log.info("Bot token -- {}", value)

    lazy val GetUpdatesUri: Uri = Uri(s"/bot$value/getUpdates")
    lazy val SendMessageUri: Uri = Uri(s"/bot$value/sendMessage")
  }

  /**
    * Https flow to telegram.
    *
    * @param as actor system
    * @param mt materializer
    * @return request to response flow
    */
  def httpFlow(implicit as: ActorSystem, mt: Materializer): Flow[TgReq, TgRes, Any] = {
    val uri: String = "api.telegram.org"
    log.debug("create Telegram API flow {}", uri)
    Http().newHostConnectionPoolHttps[TgMethod](uri)
  }
}




