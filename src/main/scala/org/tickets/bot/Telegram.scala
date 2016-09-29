package org.tickets.bot

import java.util.{Locale, ResourceBundle}

import akka.actor.{Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.tickets.api.TextMsg
import org.tickets.bot.Telegram._
import org.tickets.misc.Log

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

/**
  * Telegram API.
  */
class Telegram(val flow: Flow[(HttpRequest, TelegramMethod), (Try[HttpResponse], TelegramMethod), _],
               implicit val mt: Materializer, val telegramMethods: TelegramMethods) extends Actor with Log with Json4sSupport {

  import akka.http.scaladsl.unmarshalling._
  import context.dispatcher
  import org.json4s._
  import scala.concurrent.duration._

  implicit val sz: Serialization = jackson.Serialization
  implicit val fs: Formats = DefaultFormats

  private val tick = context.system.scheduler.schedule(
    initialDelay = 1.second,
    interval = 10.seconds,
    receiver = self,
    message = GetUpdates
  )


  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    tick.cancel()
  }

  override def receive: Receive = {
    case GetUpdates =>
      log.debug("receive#GetUpdates")
      pullUpdates onSuccess {
        case (response, method) => response match {
          case Success(httpResponse) =>
            val resp = Unmarshal(httpResponse.entity).to[JArray]
            println(resp)
          case Failure(error) => log.error("Failed", error)
        }
      }

    case push @ PushMsg(code, chatId, local) =>
      val text = ResourceBundle.getBundle("Bot", local).getString(code)
      val msg = TextMsg(chat = chatId, text = text)
      log.debug("receive#{}", push)
  }

  private def pullUpdates: Future[(Try[HttpResponse], TelegramMethod)] =
    Source.single(
      telegramMethods.getUpdates() -> null
    ).via(flow).runWith(Sink.head)

}

object Telegram {
  /**
    * Telegram API method.
    */
  type TelegramMethod = Method

  sealed trait Method {
    def apply[T: ToEntityMarshaller](content: Option[T] = None)(implicit  ec: ExecutionContext): HttpRequest
    def apply[T: ToEntityMarshaller](implicit  ec: ExecutionContext): HttpRequest = this.apply(None)
  }

  case class GetUpdates(token: String) extends Method {
    override def apply[T: ToEntityMarshaller](content: Option[T])(implicit ec: ExecutionContext): HttpRequest =
      RequestBuilding.Get(s"/bot$token/getUpdates", HttpEntity.empty(ContentTypes.`application/json`))
  }

  case class SendMsg(token: String) extends Method {
    override def apply[T: ToEntityMarshaller](content: Option[T])(implicit ec: ExecutionContext): HttpRequest =
      RequestBuilding.Post(s"/bot$token/sendMessage", content)
  }

  case class TelegramMethods(token: String) {
    private lazy val GetUpdates = GetUpdates(token)

    def getUpdates: Method = GetUpdates

    def sendMessage: Method = SendMsg(token)
  }


  /**
    * HTTPS host based flow. Use bot.api.host value from configuration.
    * @param cfg app config
    * @return https flow
    */
  def https(cfg: Config)(implicit ac: ActorSystem, mt: Materializer): Flow[(HttpRequest, TelegramMethod), (Try[HttpResponse], TelegramMethod), _] = {
    Http().newHostConnectionPoolHttps[TelegramMethod](cfg.getString("bot.api.host"))
  }

  case object PullUpdates

  /**
    * Send message to client.
    * @param msgCode l19n code
    * @param local locale
    */
  case class PushMsg(msgCode: String, chatId: Long, local: Locale = Locale.ENGLISH)

}