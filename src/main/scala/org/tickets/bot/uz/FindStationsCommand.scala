package org.tickets.bot.uz

import akka.actor.ActorRef
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.JArray
import org.json4s._
import org.tickets.bot.uz.FindStationsCommand.{FindStations, StationHits}
import org.tickets.misc.{ActorSlf4j, Command, HttpSupport}

import scala.concurrent.ExecutionContext

object FindStationsCommand {

  /**
    * Ask API for stations.
    * @param like like name
    */
  final case class FindStations(like: String)

  /**
    * Found stations in API
    * @param stations stations
    */
  final case class StationHits(stations: List[Station])

}

/**
  * Command that will be executed on successful http response to UZ Api.
  *
  * @param sender producer of this command.
  * @author Bogdan_Snisar
  */
final case class FindStationsCommand(sender: ActorRef)
  extends Command[FindStations] with Json4sSupport with UzCommand with ActorSlf4j {

  import HttpSupport.Json4sImplicits._

  override def exec(httpResponse: HttpResponse)(
    implicit
    mt: Materializer,
    ec: ExecutionContext): Unit = {

    Unmarshal(httpResponse.entity).to[JValue]
      .map(json => parseUzAnswer(json))
      .onFailure { case ex => log.error("Unmarshalling failed", ex) }
  }

  override def onContent(data: JValue): Unit = data match {
    case JArray(stations) =>
      val foundStations = stations
        .foldLeft(List.empty[Station]) { (list, data) =>
          import Station._
          val station = data.as[Station]
          station :: list
        }

      sender ! StationHits(foundStations)

    case data @ _ =>
      log.error("Unknown json structure. Expect {'value': [...]}, but was {}", data)
  }
}