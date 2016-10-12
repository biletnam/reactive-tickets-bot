package org.tickets.telegram

import akka.actor.{Actor, ActorRef}
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.tickets.misc.LogSlf4j
import org.tickets.telegram.Pull.{Ack, NotFetch, Tick}
import org.tickets.telegram.Telegram.{BotToken, HttpFlow}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

object Pull {
  case object Tick
  case object NotFetch
  case class Ack(seqNum: Int)
}

class Pull(val httpFlow: HttpFlow,
           val token: BotToken,
           val mt: Materializer, dest: ActorRef) extends Actor with LogSlf4j {

  import context.dispatcher

  /**
    *
    */
  private var offset: Int = 0

  override def receive: Receive = pulling()

  def ack(): Receive = {
    case Ack(seqNum) =>
      offset = seqNum
      context become pulling()
    case NotFetch =>
      context become pulling()
  }

  def pulling(): Receive = {
    case Tick =>
      fetchUpdates.onComplete(deliverUpdatesCallback)
      context become ack()
  }

  /**
    * Send updates to destination or recall updates again.
    * @param tryUpdates result of computation
    */
  def deliverUpdatesCallback(tryUpdates: Try[Updates]): Unit = tryUpdates match {
    case Success(updates) =>
      log.debug("#pulling fetched next updates[{}], resend to [{}]", updates.size, dest)
      dest ! updates
    case Failure(ex) =>
      log.error("#pulling failed", ex)
      self ! NotFetch
  }


  /**
    * Call host for updates and pars it into [[Updates]]
    * @return async result of updates.
    */
  def fetchUpdates: Future[Updates] = {
    import UpdatesJVal._
    implicit val materializer = mt


    Source.single(GetUpdates(token))
      .via(httpFlow)
      .mapAsync(1) {
        case (Success(httpResponse), method) =>
          Unmarshal(httpResponse.entity).to[Updates]
        case (Failure(error), method) =>
          Future.failed(error)
      }.runWith(Sink.head)
  }

}
