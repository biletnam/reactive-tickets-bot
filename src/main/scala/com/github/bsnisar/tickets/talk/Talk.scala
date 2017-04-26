package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.telegram.actor.TelegramPush
import com.typesafe.scalalogging.LazyLogging
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update._

import scala.util.{Failure, Success}
import scala.util.matching.Regex


object Talk {
  def props(charID: String,  stationId: StationId, telegram: ActorRef): Props =
    Props(classOf[Talk], charID, stationId, telegram)

  val ArrivalTime: Regex = "^/arrive\\s.*".r
  val DepartureTime: Regex = "^/departure\\s.*".r


}

class Talk(val chatID: String,
           val stationId: StationId,
           val notifyRef: ActorRef) extends Actor with LazyLogging {

  var talkEntity: TalkEntity = Default()

  override def receive: Receive = {
    case update @ Text(StationId.StationsPointerCommands(_*)) =>
      stationId.decode(update.text) match {
        case Success(parsedID) =>
          if (parsedID.from) {
            talkEntity = talkEntity.withDepartureFrom(parsedID.id)
          } else {
            talkEntity = talkEntity.withArriveTo(parsedID.id)
          }

          notifyRef ! UpdatesNotifier.AcceptNotify(update.seqNum)
        case Failure(ex) => logger.error("failed to parse id command", ex)
      }

    case update @ Text(Talk.ArrivalTime()) =>
      talkEntity = talkEntity.withArrive(update.text)
    case update @ Text(Talk.DepartureTime()) =>
      talkEntity = talkEntity.withDeparture(update.text)
  }
}
