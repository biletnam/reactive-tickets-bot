package org.tickets.api

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import com.fasterxml.jackson.databind.JsonNode
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import org.tickets.misc.Log
import org.tickets.msg.telegram.TgUpdatesJsonNode

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Created by bsnisar on 18.09.16.
  */
class TelegramUpdates(val connection: HttpStream,
                      val materializer: Materializer,
                      val handlerRef: ActorRef) extends Log with JacksonSupport  {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.collection.JavaConversions._

  private implicit val _mt = materializer

  def startMessagePooling(): Unit = {
    val source: Future[(Try[HttpResponse], Int)] = connection.stream.runWith(Sink.head)
    source.map {
      case ((Success(response), _)) =>
        val update: Future[JsonNode] = Unmarshal.apply(response).to[JsonNode]
        log.debug("GET /getMessage {}", response.status)
        update.onComplete(consumeUpdate)
      case ((Failure(error), _)) =>
        log.error("GET /getMessage failed: {}", error.getMessage)
    }
  }

  private def consumeUpdate(data: Try[JsonNode]) = data match {
    case Success(msg) if msg.isArray =>
      new TgUpdatesJsonNode(msg).toIterable foreach( handlerRef ! _)

    case Failure(err) => log.error("Unmarshal failed", err)
  }

}
