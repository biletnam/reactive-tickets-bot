package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{ActorRef, FSM}
import org.tickets.UserInteractions
import org.tickets.bot.QuerySpec._
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand


object QuerySpec {
  private def groupStations(stations: Seq[Station]): Map[String, Station] = {
    val seed = ThreadLocalRandom.current().nextInt(10000)
    stations.zip(1 to stations.size).foldLeft(Map.empty[String, Station])((acc, idxStation) => {
      val id = s"st_${Integer.toHexString(seed + idxStation._2)}"
      acc + (id -> idxStation._1)
    })
  }

  import Todo._

  sealed trait QueryStatus
  case object WaitInput extends QueryStatus
  case object WaitApiSearch extends QueryStatus
  case object WaitClientChoice extends QueryStatus
  case object Ready extends QueryStatus


  sealed trait Param
  case object EmptyParam extends Param
  final case class Query(
    todo: Action = Todo.Dest, remain: List[Action] = Todo.Src :: Todo.ArriveAt :: Nil,
    done: Map[Action, Any] = Map.empty) extends Param {

    def completActive(data: Any): Query = {
      val doneTasks = done + (todo -> data)
      if (remain.isEmpty)
        copy(todo = Todo.AllDone, done = doneTasks)
      else
        copy(todo = remain.head, remain = remain.tail, done = doneTasks)
    }

  }
  final case class Choices(data: Map[String, Station], query: Query) extends Param
}

object Todo extends Enumeration {
  type Action = Value
  val Dest, Src, ArriveAt, AllDone = Value
}

class QuerySpec(uz: ActorRef, parent: ActorRef) extends FSM[QueryStatus, Param] {

  startWith(WaitInput, EmptyParam)


  when(WaitInput) {
    case Event(name: String, EmptyParam) =>
      uz ! FindStationsCommand(self, name)
      goto(WaitApiSearch) using Query()

    case Event(name: String, q: Query) if q.todo == Todo.Src =>
      uz ! FindStationsCommand(self, name)
      goto(WaitApiSearch) using q

    case Event(name: String, q: Query) if q.todo == Todo.ArriveAt =>
      stay()
  }

  when(WaitApiSearch) {
    case Event(FindStationsCommand.StationHits(stations), q: Query) =>
      val variants = groupStations(stations)
      parent ! UserInteractions.PickUpStation(variants)
      goto(WaitClientChoice) using Choices(variants, q)
  }

  when(WaitClientChoice) {
    case Event(id: String, Choices(vars, q: Query)) if vars.contains(id) =>
      goto(WaitInput) using q.completActive(vars(id))
  }

  onTransition {
    case WaitClientChoice -> WaitInput =>
      nextStateData match {
        case Query(Todo.Src, _, _) =>
          parent ! UserInteractions.NeedArrivalState
        case Query(Todo.AllDone, _, _) =>
          goto(Ready)
      }
    case WaitInput -> WaitInput =>
      parent ! UserInteractions.NeedDepartureStation
  }

  initialize()
}
