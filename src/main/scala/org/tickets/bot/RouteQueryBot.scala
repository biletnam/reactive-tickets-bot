package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef}
import org.tickets.UserInteractions
import org.tickets.bot.RouteQueryBot.{Departure, StationType}
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

object RouteQueryBot {
  private trait StationType
  private case object Departure extends StationType
  private case object Arrival extends StationType


  private def groupStationsById(stations: Seq[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(5000)
    stations.foldLeft(Map.empty[String, Station]) { (acc, st) =>
      val id: String = s"st_${Integer.toHexString(seed)}"
      val pair: (String, Station) = id -> st
      acc + pair
    }
  }

}

class RouteQueryBot(client: ActorRef, uz: ActorRef) extends Actor {

  override def receive: Receive = needDepartureStation()

  /**
    * Need name for search for departure station.
    */
  def needDepartureStation(): Receive = {
    case likePattern: String =>
      uz ! FindStationsCommand(self, likePattern)
      context become waitApiAnswer(Departure)
  }

  /**
    * Wait response from API.
    */
  def waitApiAnswer(stationType: StationType): Receive = {
    case FindStationsCommand.StationHits(stations) =>
      context become askClientForStation(Map.empty, stationType)

    case FindStationsCommand.StationHits(stations) if stations.isEmpty =>
      println("B")
    case FindStationsCommand.SearchError =>
      println("C")
  }

  /**
    * Ask client for picking up correct station.
    */
  def askClientForStation(variants: Map[String, Station], stationType: StationType): Receive = {
    case id: String if variants.contains(id) =>

  }
}

