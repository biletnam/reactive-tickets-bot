package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{ActorRef, FSM}
import org.tickets.bot.QuerySpec._
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand


object QuerySpec {
  trait QueryStatus
  case object NeedDest extends QueryStatus
  case object WaitDest extends QueryStatus
  case object WaitClientAnswer extends QueryStatus
  case object Ready extends QueryStatus

  trait Todo
  case object From extends Todo
  case object To extends Todo

  val Actions = From :: To :: Nil

  trait Param
  case object EmptyParam extends Param
  case class Ctx(todo: Todo, remain: List[Todo], done: Map[Todo, Any]) extends Param
  case class StationsVars(variants: Map[String, Station], param: Param) extends Param

  def groupStations(stations: Seq[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(10000)
    stations.zip(1 to stations.size).foldLeft(Map.empty[String, Station])((acc, idxStation) => {
        val id = s"st_${Integer.toHexString(seed + idxStation._2)}"
        acc + (id -> idxStation._1)
    })
  }

}

class QuerySpec(uz: ActorRef, parent: ActorRef) extends FSM[QueryStatus, Param] {

  when(NeedDest) {
    case Event(likeName: String, EmptyParam) =>
      uz ! FindStationsCommand(self, likeName)
      goto(WaitDest) using Ctx(From, To :: Nil, Map.empty)
  }

  when(WaitDest) {
    case Event(FindStationsCommand.StationHits(stations), param) =>
      goto(WaitClientAnswer) using StationsVars(groupStations(stations), param)
  }


  when(WaitClientAnswer) {
    case Event(id: String, d @ StationsVars(vars: Map[String, Station], ctx: Ctx)) if vars.contains(id) =>
      val station = vars(id)
      goto(Ready) using ctx.copy(done = ctx.done + (ctx.todo -> station))
    case Event(id: String, ctx) =>
      stay() using ctx
  }

  onTransition {
    case _ -> Ready =>
      nextStateData match {
        case ctx @ Ctx(_, To :: rest, _) =>
          parent ! ""
          goto(NeedDest) using ctx.copy(todo = To, remain = rest)

        case Ctx(todo, pending, done) if pending.isEmpty =>

      }
  }

}
