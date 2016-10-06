package org.tickets.bot.tg

import javax.inject.Inject

import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, Uri}
import com.typesafe.config.Config
import org.tickets.misc.ActorSlf4j

import scala.concurrent.ExecutionContext

object TelegramMethod extends ActorSlf4j {
 /*
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
  def sendMessage[T: ToEntityMarshaller](content: T)(implicit tk: BotToken, ec: ExecutionContext): HttpRequest =
    RequestBuilding.Post(tk.SendMessageUri, content)

*/

}

