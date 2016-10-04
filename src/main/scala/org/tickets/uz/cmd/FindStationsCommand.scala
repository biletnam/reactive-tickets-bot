package org.tickets.uz.cmd

import java.net.URLEncoder

import akka.actor.ActorRef
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import com.google.common.base.Charsets
import de.heikoseeberger.akkahttpjson4s.Json4sSupport
import org.json4s.JsonAST.JArray
import org.json4s._
import org.tickets.misc.HttpSupport.Request
import org.tickets.misc.{ActorSlf4j, HttpSupport}
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand.StationHits

import scala.concurrent.ExecutionContext

object FindStationsCommand {

  def request(nameLike: String, ref: ActorRef): Request = {
    val encName = URLEncoder.encode(nameLike, Charsets.UTF_8.name())
    val get: HttpRequest = RequestBuilding.Get(s"/ru/purchase/station/$encName/")
    get -> FindStationsCommand(ref)
  }

  /**
    * Found stations in API
    * @param stations stations
    */
  final case class StationHits(stations: List[Station])

  /**
    * API search problem.
    */
  final case object SearchError
}

/**
  * Command that will be executed on successful http response to UZ Api.
  *
  * @param sender producer of this command.
  * @author Bogdan_Snisar
  */
final case class FindStationsCommand(sender: ActorRef, name: String = null)
  extends Command with Json4sSupport with UzCommand with ActorSlf4j {

  import HttpSupport.Json4sImplicits._

  override def exec(httpResponse: HttpResponse)(
    implicit
    mt: Materializer,
    ec: ExecutionContext): Unit = {
      Unmarshal(httpResponse.entity).to[JValue]
        .map(json => parseUzAnswer(json))
        .onFailure { case ex => log.error("Unmarshal failed", ex) }
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