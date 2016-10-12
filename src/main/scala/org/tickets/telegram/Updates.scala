package org.tickets.telegram


import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.unmarshalling._


/**
  * Batched updates.
 *
  * @author bsnisar
  */
trait Updates {

  /**
    * Max seq number among updates.
    * @return max seq number
    */
  def lastId: Int

  /**
    * All available updates
    * @return updates or empty iterable
    */
  def updates: Iterable[Update]

  /**
    * Number of updates in batch.
    * @return number of updates in batch
    */
  def size: Int = updates.size
}

case class UpdatesJVal(updates: List[Update]) extends Updates {
  private lazy val maxId: Int = updates.view.map(_.id).max

  override def lastId: Int = maxId
}

object UpdatesJVal {
  import org.json4s._
  import org.tickets.misc.JsonUtil._
  import org.json4s.jackson.JsonMethods._

  implicit object UpdatesReader extends Reader[Updates] {
    override def read(value: JValue): Updates = {
      val errorMark = (value \ "ok").extract[Boolean]
      if (errorMark) {
        throw new IllegalStateException("response marked as failed")
      }

      val content = value \ "response"
      content match {
        case JArray(messages) =>
          UpdatesJVal(messages.map(new UpdateJVal(_)))
        case _ =>
          throw new IllegalStateException("expect {.., 'response': [...]} format")
      }

    }
  }

  implicit def updatesByJson4s: FromEntityUnmarshaller[Updates] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset { (data, charset) =>
        val json = parse(data.decodeString(charset.nioCharset.name))
        json.as[Updates]
      }
}