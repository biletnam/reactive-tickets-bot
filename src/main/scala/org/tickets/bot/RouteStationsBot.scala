package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.misc.ActorSlf4j
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

object RouteStationsBot {



  def props(uz: ActorRef, tg: ActorRef): Props = Props(classOf[RouteStationsBot], uz, tg)
}

class RouteStationsBot(uz: ActorRef, tg: ActorRef) extends Actor with ActorSlf4j {
  override def receive: Receive = idle()

  def idle(): Receive = {
    case Bot.ReqDepartureStation(name) =>
      log.debug("#idle: request for departure station {}", name)
      uz ! FindStationsCommand(self, name)
      context become waitApiAnswer()
  }

  def waitApiAnswer(): Receive = {
    case FindStationsCommand.StationHits(variants) =>
      log.debug("#waitApiAnswer: get api search result and ask client")
      tg ! Bot.AskDepartureStationsFrom(groupChoices(variants))
      context become waitApiAnswer()
  }

  def waitUserAnswer(): Receive = {
    ???
  }

  private def groupChoices(stations: List[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(100000)
    val pairs: Seq[(String, Station)] = stations.zip(1 to stations.size).map {
      case (station, idx) => s"st/${Integer.toHexString(seed + idx)}" -> station
    }

    pairs.toMap
  }

}
