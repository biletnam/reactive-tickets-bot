package org.tickets.api

import akka.actor.Actor
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.stream.Materializer
import akka.stream.scaladsl.Source
import org.tickets.api.Telegram.MsgSelection
import org.tickets.misc.{Log, Named}

import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
  * Telegram API.
  *
  * @param telegramHost telegram api root.
  */
class Telegram(val telegramHost: String,
               implicit val materializer: Materializer) extends Actor with Log {
  private implicit val system = context.system

  val poolClientFlow = Http().cachedHostConnectionPoolHttps[Int](host = telegramHost)

  @scala.throws[Exception](classOf[Exception])
  def startMessagePooling(): Unit = {
    val src = Source
      .tick(initialDelay = 5.seconds,
        interval = 30.seconds,
        tick = HttpRequest(uri = "/getMessage") -> 42
      )

    log.info("Telegram#preStart(): init pooling {}", src)
    src.via(poolClientFlow)
      .map {
        case ((Success(response), _)) =>
          log.debug("[TelegramApi] {} /getMessage", response.status)
//          Unmarshal.apply(response).to[]

        case ((Failure(error), _)) =>
          log.error("[TelegramApi] GET /getMessage failed: {}", error.getMessage)
      }
  }

  override def receive: Receive = {
    case MsgSelection =>

  }
}

object Telegram extends Named {
  override val name: String = "telegram"

  case object MsgSelection

}
