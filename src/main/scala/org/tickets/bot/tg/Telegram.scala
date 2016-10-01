package org.tickets.bot.tg

import java.util.{Locale, ResourceBundle}

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import com.typesafe.config.Config
import org.tickets.bot.tg.Telegram._
import org.tickets.bot.tg.TelegramMethod.BotToken
import org.tickets.misc.HttpSupport.{Bound, EmptyContext, Request, Response}
import org.tickets.misc.Log

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


object Telegram {

  case object GetUpdatesTick

  /**
    * HTTPS host based flow. Use bot.api.host value from configuration.
    * @param cfg app config
    * @return https flow
    */
  def https(cfg: Config)(implicit ac: ActorSystem, mt: Materializer): Flow[Request, Response, HostConnectionPool] = {
    Http().newHostConnectionPoolHttps[Bound](cfg.getString("bot.api.host"))
  }

  /**
    * Actor Props.
    * @return constructed props.
    */
  def props(flow: Flow[Request, Response, _], mt: Materializer, botToken: BotToken): Props = {
    Props(classOf[Telegram], flow, mt, botToken)
  }
}

/**
  * Telegram API.
  */
class Telegram(val flow: Flow[Request, Response, _],
               implicit
               val mt: Materializer,
               implicit
               val botToken: BotToken) extends Actor with Log with Json4sSupport {

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
    message = GetUpdatesTick
  )

  @scala.throws[Exception](classOf[Exception])
  override def postStop(): Unit = {
    tick.cancel()
  }

  override def receive: Receive = {
    case GetUpdatesTick =>
      log.debug("receive#GetUpdates")
      consumeTelegramMessages()
  }

  private def consumeTelegramMessages(): Unit = {
    val pullResponse: Future[Response] = Source.single(
      TelegramMethod.getUpdates -> EmptyContext
    ).via(flow).runWith(Sink.head)

    pullResponse onSuccess {
      case (maybeResponse, method) => maybeResponse match {
        case Success(response) =>
          val messages = Unmarshal(response.entity).to[JArray]
          println(messages)
        case Failure(error) => log.error("Failed", error)
      }
    }
  }
}

