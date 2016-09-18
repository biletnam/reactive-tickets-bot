package org.tickets.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.tickets.misc.Log
import org.tickets.msg.telegram.Update

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by bsnisar on 18.09.16.
  */
class TelegramUpdates(val connection: HttpStream,
                      val materializer: Materializer,
                      val handlerRef: ActorRef)
  extends Log with JacksonSupport  {

  import scala.concurrent.ExecutionContext.Implicits.global
  private implicit val _mt = materializer

  def startMessagePooling(): Unit = {
    val source: Future[(Try[HttpResponse], Int)] = connection.stream.runWith(Sink.head)
    source.map {
      case ((Success(response), _)) =>
        val update: Future[List[Update]] = Unmarshal.apply(response).to[List[Update]]
        log.debug("GET /getMessage {}", response.status)
        update.onComplete(consumeUpdate)
      case ((Failure(error), _)) =>
        log.error("GET /getMessage failed: {}", error.getMessage)
    }
  }

  private def consumeUpdate(data: Try[List[Update]]) = data match {
    case Success(msg) => msg foreach (handlerRef ! _)
    case Failure(err) => log.error("Unmarshal failed", err)
  }

}
