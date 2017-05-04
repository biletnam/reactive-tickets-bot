package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.misc.StationId
import com.github.bsnisar.tickets.telegram.Update
import com.github.bsnisar.tickets.telegram.Update._
import com.github.bsnisar.tickets.telegram.{MsgCommandFailed, MsgHello, MsgQueryExecute, MsgQueryUpdate}
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

  override def receive: Receive = ???
}
