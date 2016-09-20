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
class PoolTelegramUpdates(val connection: HttpStream,
                          val materializer: Materializer,
                          val handlerRef: ActorRef) extends Log with JacksonSupport  {

  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.collection.JavaConversions._

  private implicit val _mt = materializer

  def startMessagePooling(): Unit = {
    connection.stream.map {
      case (resp, seq) =>
    }.runWith(Sink.head)
  }

  private def consumeUpdate(data: Try[JsonNode]) = data match {
    case Success(msg) if msg.isArray =>
    case Failure(err) => log.error("Unmarshalling failed", err)
  }

}
