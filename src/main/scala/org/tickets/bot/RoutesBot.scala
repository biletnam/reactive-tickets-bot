package org.tickets.bot

import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef, Props}
import org.tickets.bot.RoutesBot._
import org.tickets.misc.ActorSlf4j
import org.tickets.uz.Station
import org.tickets.uz.cmd.FindStationsCommand

object RoutesBot {

  def props(uz: ActorRef, tg: ActorRef): Props =
    Props(classOf[RoutesBot], uz, tg)

  trait Value
  case class StationValue(station: Station) extends Value
  case class StationVariants(map: Map[String, Station]) extends Value

  object StationVariants {
    def apply(stations: List[Station]): StationVariants
    = StationVariants(groupStations(stations))

    private def groupStations(list: List[Station]): Map[String, Station] = {
      val seed = ThreadLocalRandom.current().nextInt(5000)
      list.zip(1 until list.size).map {
        case (station, idx) => java.lang.Long.toString(seed + idx, 32) -> station
      } toMap
    }
  }


  trait Part
  case object Arrival extends Part
  case object Departure extends Part
  case object ArriveAt extends Part

  /**
    * State of FSM.
    * @param todo actions that should be defined
    * @param req currect request
    */
  protected final case class State(todo: List[Part], req: Map[Part, Value]) {

    def withDoneTodo: State = {
      this.copy(todo = todo.tail)
    }

    def withUndoneTodo(part: Part) = {
      copy(todo = part :: todo)
    }
  }

  protected object State {
    def initial: State = State(
      Arrival :: Departure :: ArriveAt :: Nil,
      Map.empty
    )
  }
}

class RoutesBot(uz: ActorRef, telegram: ActorRef) extends Actor with ActorSlf4j {
  override def receive: Receive = idle(State.initial)

  def idle(state: State): Receive = {
    case ReqRouteDeparture(name) =>
      log.debug("#idle: need departure station for {} name", name)
      uz ! FindStationsCommand(self, name)
      context become waitStationsSearchOf(Departure, state)
    case ReqRouteArrival(name) =>
      log.debug("#idle: need arrival station for {} name", name)

      //TODO:  ask API for station with this name
    context become waitStationsSearchOf(Arrival, state)
  }

  def waitStationsSearchOf(part: Part, state: State): Receive = {
    case FindStationsCommand.StationHits(variants) =>
      log.debug("#waitStationsSearchOf: found {} variants for a search of {}", variants.size, part)
      val newReq = state.req + (part -> StationVariants(variants))
      //TODO:  ask client for picking up
      context become askClientAnswer(part, state.copy(req = newReq))
    case FindStationsCommand.SearchError =>
      log.error("#waitStationsSearchOf: api respond with SearchError")
      // TODO: push error message
      // discard state
      context become idle(state.withUndoneTodo(part))
  }

  def askClientAnswer(part: Part, state: State): Receive = {
    case RouteDeparturePicked(id) =>
      state.req(part) match {
        case StationVariants(options) if options.contains(id) =>
          val newReq = state.req + (part -> StationValue(options(id)))
          moveForward(state.copy(req = newReq))
        case _ =>
          log.debug("#askClientAnswer: selected id not satisfy for a state")
      }
  }

  def moveForward(state: State): Unit = {
    val next: Part = state.todo.head
    log.debug("#moveForward: next todo is {}", next)
    next match {
      case Departure => ???
        //TODO: ask client for input
        // idle for input
        context become idle(state.withDoneTodo)
      case Arrival => ???
        //TODO: ask client for input

        // idle for input
        context become idle(state.withDoneTodo)
      case ArriveAt => ???
      //TODO: ask client for input
    }
  }



}
