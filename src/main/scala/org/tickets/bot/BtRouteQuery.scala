package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef, ActorSelection, Props}
import org.tickets.UserInteractions
import org.tickets.bot.BtRouteQuery.{Arrival, Departure, Param}
import org.tickets.misc.LogSlf4j
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

object BtRouteQuery {

  def props(parent: ActorRef): Props = Props(classOf[BtRouteQuery], parent)

  private trait Param
  private case object Departure extends Param
  private case object Arrival extends Param

  private def groupStationsById(stations: Seq[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(5000)
    stations.zip(1 to stations.size).foldLeft(Map.empty[String, Station]) { (acc, idxStation) =>
      val id: String = s"st_${Integer.toHexString(seed + idxStation._2)}"
      val pair: (String, Station) = id -> idxStation._1
      acc + pair
    }
  }


}

class BtRouteQuery(client: ActorRef) extends Actor with LogSlf4j {

  private val uz: ActorSelection = context.actorSelection("/railway")

  override def receive: Receive = needStation(Departure)

  private var query: Map[Param, Any] = Map.empty

  private val todo = List(needStation(Departure), needStation(Arrival))

  /**
    * Need name for search for departure station.
    */
  def needStation(param: Param): Receive = {
    case likePattern: String =>
      uz ! FindStationsCommand(self, likePattern)
      context become waitApiAnswer(param)
  }

  /**
    * Wait response from API.
    */
  def waitApiAnswer(param: Param): Receive = {
    case FindStationsCommand.StationHits(stations) =>
      context become waitClientReplyForStation(BtRouteQuery.groupStationsById(stations), param)
    case FindStationsCommand.StationHits(stations) if stations.isEmpty =>
      log.debug("Empty response")
    case FindStationsCommand.SearchError =>
      log.error("Api unavailable")
  }

  /**
    * Ask client for picking up correct station.
    */
  def waitClientReplyForStation(variants: Map[String, Station], param: Param): Receive = {

    case id: String if variants.contains(id) =>
      query = query + (param -> variants(id))
      client ! UserInteractions.NeedArrivalStation
      context become needStation(Arrival)

    case msg: String =>
      log.warn("unexpected message ({}) from client", msg)
  }
}

