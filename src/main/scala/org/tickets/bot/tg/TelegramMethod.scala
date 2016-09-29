package org.tickets.bot.tg

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, Uri}

object TelegramMethod {

  /**
    * Telegram method bot token.
    * @param value token value
    */
  case class BotToken(value: String) {
    lazy val GetUpdatesUri: Uri = Uri(s"/bot$value/getUpdates")
    lazy val SendMessageUri: Uri = Uri(s"/bot$value/sendMessage")
  }

  /**
    * <a href="https://core.telegram.org/bots/api#getupdates">Get updates</a>
    * @param tk implicit bot token.
    * @return ready http request.
    */
  def getUpdates(implicit tk: BotToken): HttpRequest =
    RequestBuilding.Get(tk.GetUpdatesUri, HttpEntity.empty(ContentTypes.`application/json`))

  /**
    * Prepare <a href="https://core.telegram.org/bots/api#sendmessage">sendMessage</a> request.
    * @param content conent
    * @param tk implicit token
    * @tparam T type of content
    * @return ready http POST request.
    */
  def sendMessage[T: ToEntityMarshaller](content: T)(implicit tk: BotToken): HttpRequest =
    RequestBuilding.Post(tk.SendMessageUri, content)



}

