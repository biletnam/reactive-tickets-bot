package org.tickets.bot

import java.time.LocalDate

import akka.actor.{ActorRef, FSM, Props}
import org.tickets.bot.QuerySpec._
import org.tickets.misc.UniqueIndex
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

import scala.concurrent.duration._

object QuerySpec {
  import Todo._


  def props(parent: ActorRef, uz: ActorRef): Props =
    Props(classOf[QuerySpec], uz, parent)


  sealed trait QueryStatus
  case object Idle extends QueryStatus
  case object WaitInput extends QueryStatus
  case object WaitApiSearch extends QueryStatus
  case object WaitClientChoice extends QueryStatus
  case object Ready extends QueryStatus

  /**
    * Partial param for query
    * @author Bogdan_Snisar
    */
  sealed trait Param

  /**
    * Empty query.
    * @author Bogdan_Snisar
    */
  case object EmptyParam extends Param

  /**
    * Partially build query.
    * @param todo current param that mast be defined
    * @param remain remain actions, that have to be done
    * @param done already defined parameters
    * @author Bogdan_Snisar
    */
  final case class PartialData(todo: Action = Todo.Dest,
                               remain: List[Action] = Todo.Src :: Todo.ArriveAt :: Nil,
                               done: Map[Action, Any] = Map.empty) extends Param {

    /**
      * Complete action.
      * @param data data for completion.
      * @return partially data.
      */
    def completeAction(data: Any): PartialData = {
      val doneTasks = done + (todo -> data)
      if (remain.isEmpty)
        copy(todo = Todo.AllDone, done = doneTasks)
      else
        copy(todo = remain.head, remain = remain.tail, done = doneTasks)
    }
  }

  final case class TmpChoices(data: Map[String, Station], query: PartialData) extends Param
}

object QueryProtocol {
  case object Start
  case object Reload
  case class DefQuery(from: Station, to: Station, arriveAt: LocalDate)
}

/**
  * Actions that have to be done.
  * @author Bogdan_Snisar
  */
object Todo extends Enumeration {
  type Action = Value
  val Dest, Src, ArriveAt, AllDone = Value
}

class QuerySpec(uz: ActorRef, parent: ActorRef) extends FSM[QueryStatus, Param] with UniqueIndex {

  startWith(Idle, EmptyParam)

  when(Idle) {
    case Event(QueryProtocol.Start, param) =>
      parent ! UserInteractions.NeedDepartureStation
      goto(WaitInput) using param
  }

  /**
    * Wait user input.
    */
  when(WaitInput) {
    // input for departure station
    case Event(name: String, EmptyParam) =>
      uz ! FindStationsCommand(self, name)
      goto(WaitApiSearch) using PartialData()

    // input for arrival station
    case Event(name: String, q: PartialData) if q.todo == Todo.Src =>
      uz ! FindStationsCommand(self, name)
      goto(WaitApiSearch) using q

    // input for arrival date
    case Event(name: String, q: PartialData) if q.todo == Todo.ArriveAt =>
      stay()
  }

  /**
    * Wait railway api response
    */
  when(WaitApiSearch) {
    case Event(FindStationsCommand.StationHits(stations), q: PartialData) =>
      import UniqueIndex._

      val variants: Map[String, Station] = groupBy[Station](stations)
      parent ! UserInteractions.PickUpStation(variants)
      goto(WaitClientChoice) using TmpChoices(variants, q)
  }

  /**
    * Wait client pick up.
    */
  when(WaitClientChoice, stateTimeout = 5.seconds) {

    case Event(id: String, TmpChoices(vars, q: PartialData)) if vars.contains(id) =>
      goto(WaitInput) using q.completeAction(vars(id))
  }

  onTransition {
    case WaitClientChoice -> WaitInput =>
      nextStateData match {
        // ask client for next input
        case PartialData(Todo.Src, _, _) =>
          parent ! UserInteractions.NeedArrivalState

        // complete query
        case PartialData(Todo.AllDone, _, _) =>
          goto(Idle) using EmptyParam

        case e @ _ =>
          throw new IllegalStateException(s"unexpected state $e")
      }
  }

  whenUnhandled {
    case Event(QueryProtocol.Reload, _) =>
      goto(Idle) using EmptyParam

    case Event(e, s) =>
      log.warning("received unhandled request {} in state {}/{}", e, stateName, s)
      stay
  }

  initialize()
}
