package org.tickets.bot.tg

import akka.actor.{Actor, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.HostConnectionPool
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.tickets.bot.tg.Telegram._
import org.tickets.bot.tg.TelegramMethod.BotToken
import org.tickets.misc.HttpSupport.{Bound, Request, Response}
import org.tickets.misc.{EmptyContext, ActorSlf4j}

import scala.concurrent.Future
import scala.util.{Failure, Success}


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
  * TelegramRef API.
  */
class Telegram(val flow: Flow[Request, Response, _],
               implicit
               val mt: Materializer,
               implicit
               val botToken: BotToken) extends Actor with ActorSlf4j with Json4sSupport {

  import akka.http.scaladsl.unmarshalling._
  import org.json4s._
  import org.tickets.misc.HttpSupport.Json4sImplicits._
  import context.dispatcher

  import scala.concurrent.duration._


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

