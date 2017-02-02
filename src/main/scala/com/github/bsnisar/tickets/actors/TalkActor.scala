package com.github.bsnisar.tickets.actors

import akka.actor.{FSM, Status}
import com.github.bsnisar.tickets.{Station, Stations, Update}
import com.github.bsnisar.tickets.actors.TalkActor._
import com.github.bsnisar.tickets.misc.Expressions.Expr
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._

object TalkActor {

  trait Msg
  case object Idle extends Msg
  case object SearchSearch extends Msg
  case object Qtn extends Msg

  type Talk = Map[Symbol, Any]

  object Words {
    def unapply(arg: Update): Option[List[String]] = Option(arg.text.split(" ").toList)
  }

  case class SearchStations(word: String, t: Iterable[Station])
  case class Failed(t: Throwable)
}

class TalkActor(private val stations: Stations) extends FSM[Msg, Talk] with LazyLogging {
  import akka.pattern.pipe
  import scala.concurrent.ExecutionContext.Implicits.global

  when(Idle) {
    case Event(Words("/from" :: name :: Nil), _)  =>
      stations.stationsByName(name).pipeTo(self)
      goto(SearchSearch)
  }

  when(SearchSearch, stateTimeout = 15.seconds) {
    case Event(Status.Failure(er), _) => ???
  }

}
