package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{ActorRef, FSM}
import org.tickets.bot.QuerySpec._
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand


object QuerySpec {
  trait QueryStatus
  case object NeedDest extends QueryStatus
  case object NeedSrc extends QueryStatus
  case object WaitDest extends QueryStatus
  case object WaitSrc extends QueryStatus
  case object WaitClientAnswer extends QueryStatus

  trait Param
  case object EmptyParam extends Param
  case class StationsVars(variants: Map[String, Station], remain: Param) extends Param

  def groupStations(stations: Seq[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(10000)
    stations.zip(1 to stations.size).foldLeft(Map.empty[String, Station])((acc, idxStation) => {
        val id = s"st_${Integer.toHexString(seed + idxStation._2)}"
        acc + (id -> idxStation._1)
    })
  }

}

class QuerySpec(uz: ActorRef) extends FSM[QueryStatus, Param] {

  when(NeedDest) {
    case Event(likeName: String, _) =>
      uz ! FindStationsCommand(self, likeName)
      goto(WaitDest) using EmptyParam
  }

  when(WaitDest) {
    case Event(FindStationsCommand.StationHits(stations), _) =>
      goto(WaitClientAnswer) using StationsVars(groupStations(stations), EmptyParam)
  }

  when(NeedSrc) {
    case Event(likeName: String, d) =>
      uz ! FindStationsCommand(self, likeName)
      goto(WaitSrc) using d
  }

  when(WaitClientAnswer) {
    case Event(id: String, d @ StationsVars(vars: Map[String, Station], _)) if vars.contains(id) =>
      goto(NeedSrc) using d
    case Event(id: String, d) =>
      stay() using d
  }



  onTransition {
    case _ -> NeedSrc =>
      println("Need src")
  }

}
