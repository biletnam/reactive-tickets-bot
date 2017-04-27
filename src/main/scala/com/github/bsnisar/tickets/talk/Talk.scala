package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.talk.UpdatesNotifier.AcceptNotify
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update
import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update._
import com.github.bsnisar.tickets.telegram.{MsgCommandFailed, MsgQueryExecute, MsgQueryUpdate}
import com.typesafe.scalalogging.LazyLogging

import scala.util.matching.Regex
import scala.util.{Failure, Success}


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
    case update: Update =>
      handleUpdate(update) match {
        case Left(entity) if entity.complete =>
          val msg = MsgQueryExecute(talkEntity = entity)
          runQuery()
          notifyRef ! AcceptNotify(update.seqNum, Some(update.mkReply(msg)))

        case Left(entity) =>
          val msg = MsgQueryUpdate(talkEntity = entity)
          notifyRef ! AcceptNotify(update.seqNum, Some(update.mkReply(msg)))

        case Right(_) =>
          val msg = MsgCommandFailed(cmd = update.text)
          notifyRef ! AcceptNotify(update.seqNum, Some(update.mkReply(msg)))
      }

  }

  private def handleUpdate(update: Update): Either[TalkEntity, Throwable] = update match {
    case update @ Text(StationId.StationsPointerCommands(_*)) =>
      stationId.decode(update.text) match {
        case Success(parsedID) =>
          if (parsedID.from) {
            talkEntity = talkEntity.withDepartureFrom(parsedID.id)
          } else {
            talkEntity = talkEntity.withArriveTo(parsedID.id)
          }

          Left(talkEntity)
        case Failure(ex) =>
          logger.error("failed to parse id command", ex)
          Right(ex)
      }

    case update @ Text(Talk.ArrivalTime()) =>
      talkEntity = talkEntity.withArrive(update.text)
      Left(talkEntity)
    case update @ Text(Talk.DepartureTime()) =>
      talkEntity = talkEntity.withDeparture(update.text)
      Left(talkEntity)
  }

  private def runQuery(): Unit = {

  }

}
