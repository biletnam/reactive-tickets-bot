package org.tikets.bot


import akka.actor.ActorRef
import akka.persistence.PersistentActor
import org.tikets.bot.RoutesEvents.{StationChoice, StationChoiceFrom}
import org.tikets.misc.{Destination, From, To}
import org.tikets.msg.{Msg, Phrase}


object RoutesTalk {
  sealed trait StateMark
  case object WaitingAnswer extends StateMark
}

object RoutesEvents {
  sealed trait Evt
  final case class StationChoiceFrom(matches: List[String]) extends Evt
  final case class StationChoiceTo(matches: List[String]) extends Evt


  final case class StationChoice(matches: List[String], dest: Destination) extends Evt
  final case class StationSelectionTo(index: Int) extends Evt
  final case class StationSelectionFrom(index: Int) extends Evt
}

/**
  * Talk.
  * @author bsnisar
  */
class RoutesTalk(val persistenceId: String, val stations: ActorRef) extends PersistentActor  {

  /**
    * Action of client message.
    */
  private var msgReaction : Function[Phrase, Unit] = onIdle


  override def receiveCommand: Receive = {
    case msg: Msg => msgReaction(msg.phrase)
    case Stations.MatchStationNames(names, true) =>
      persist(StationChoice(names, From)) { from =>
        askClient()
        updateState()
      }
    case Stations.MatchStationNames(names, false) =>
      persist(StationChoice(names, To)) { from =>
        askClient()
        updateState()
      }
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


  private def askClient() = {

  }

  private def updateState() = {

  }
}