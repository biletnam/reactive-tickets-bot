package com.github.bsnisar.tickets
import java.util.concurrent.atomic.AtomicInteger

import akka.http.scaladsl.client.RequestBuilding
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire
import com.typesafe.scalalogging.LazyLogging
import org.json4s.JValue
import org.json4s.JsonDSL._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

class TgUpdates(private val wire: Wire[Req, JValue])
               (implicit
                val mt: Materializer) extends Updates with Json with LazyLogging {

  private val seq = new AtomicInteger(0)

  override def pull: Future[Iterable[Update]] = {
    val offset = seq.get()
    val reqBody: JValue = "offset" -> offset
    val updatesReq = RequestBuilding.Post("getUpdates", reqBody)
    logger.debug("#pull updates with offset {}", offset)

    import ConsUpdate.Reader
    val updates: Future[Iterable[Update]] = Source.single(updatesReq -> offset)
      .via(wire.flow)
      .runWith(Sink.head)
      .map(json => json.as[Iterable[Update]])

    updates.map(_.view.map(_.id).max).onComplete {
      case Success(lastSeqNumber) => seq.addAndGet(lastSeqNumber)
      case _ => // ignore
    }

    updates
  }
}
