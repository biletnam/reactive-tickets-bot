package org.tickets.telegram


import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.unmarshalling._
import org.tickets.misc.{Logger, LogSlf4j}


/**
  * Batched updates.
 *
  * @author bsnisar
  */
trait Update {

  /**
    * Max seq number among updates.
    * @return max seq number
    */
  def lastId: Int

  /**
    * All available updates
    * @return updates or empty iterable
    */
  def messages: Iterable[Message]

  /**
    * Number of updates in batch.
    * @return number of updates in batch
    */
  def size: Int = messages.size

  /**
    * No updates are available ?
    * @return If no updates available ?
    */
  def empty: Boolean = size == 0
}

case class UpdatesJVal(messages: List[Message]) extends Update {
  private lazy val maxId: Int = messages match {
    case Nil => 0
    case _ => messages.view.map(_.id).max
  }

  override def lastId: Int = maxId
}

object UpdatesJVal extends LogSlf4j {
  import org.json4s._
  import org.tickets.misc.JsonSupport._
  import org.json4s.jackson.JsonMethods._

  implicit object UpdateReader extends Reader[Update] {
    override def read(value: JValue): Update = {
      val errorMark = (value \ "ok").extract[Boolean]
      if (!errorMark) {
        throw new IllegalStateException("response marked as failed")
      }
      val content = value \ "result"
      content match {
        case JArray(messages) =>
          UpdatesJVal(messages.map(_.as[Message]))
        case _ =>
          throw new IllegalStateException("expect {.., 'response': [...]} format")
      }

    }
  }


  implicit def fromEntityToJson4s: FromEntityUnmarshaller[Update] =
    Unmarshaller
      .byteStringUnmarshaller
      .forContentTypes(`application/json`)
      .mapWithCharset { (data, charset) =>
        val content: String = data.decodeString(charset.nioCharset.name)
        Logger.logMessage(content)
        val json = parse(content)
        json.as[Update]
      }
}