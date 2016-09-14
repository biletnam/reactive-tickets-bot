package org.tikets.bot


import akka.actor.ActorRef
import akka.persistence.PersistentActor
import org.tikets.bot.Events._
import org.tikets.bot.Stations.Station
import org.tikets.msg.{Msg, Phrase}
import org.tikets.qt.{QtStationArrive, QtStationDeparture, Question}
import org.tikets.uz.Routes


object RoutesTalk {
  sealed trait StateMark
  case object WaitingAnswer extends StateMark
}

object Events {
  sealed trait Destination
  case object From extends Destination
  case object To extends Destination


  sealed trait Evt

  final case class StationChoice(matches: Map[String, Station], dest: Destination) extends Evt
  final case class StationSelection(index: String) extends Evt
}

/**
  * Talk.
  *
  * @param persistenceId id of conversation
  * @param stations stations ref
  * @author Bogdan_Snisar
  */
class RoutesTalk(val persistenceId: String, val stations: ActorRef, var routesReq: Routes) extends PersistentActor  {

  /**
    * Action of client message.
    */
  private var msgReaction : Function[Phrase, Unit] = onIdle


  private var state: Evt = null

  override def receiveCommand: Receive = {
    case msg: Msg => msgReaction(msg.phrase)
    case Stations.MatchStationNames(names, true) => askStationSelection(names, From)
    case Stations.MatchStationNames(names, false) => askStationSelection(names, To)

  }

  override def receiveRecover: Receive = ???


  /**
    * Ready for client messages related to railway tickets
    * @param phrase phrase of a client
    */
  private def onIdle(phrase: Phrase): Unit = phrase.command match {
        case "/from" =>
          stations ! Stations.FetchStationNames("hello", true)
          msgReaction = onQuestion
        case "/to" =>
          stations ! Stations.FetchStationNames("hello", false)
          msgReaction = onQuestion
  }

  /**
    * Message must contains answer on question to client.
    * @param phrase phrase of a client
    */
  private def onQuestion(phrase: Phrase): Unit = {

  }

  /**
    * Ask client for choice.
    * @param stations stations that match request
    * @param dest destination
    */
  private def askStationSelection(stations: List[Station], dest: Destination) = {
    var choices = Map.empty[String, Station]
    for ((station, stationIdx) <- stations.zipWithIndex) {
      choices = choices + (s"/$stationIdx" -> station)
    }

    persist(StationChoice(choices, dest)) { choice =>
      updateState(choice)
      askForChoice()
    }

  }

  private def updateState(evt: Evt) = {
    state = evt
  }

  private def askForChoice() = {

  }
}