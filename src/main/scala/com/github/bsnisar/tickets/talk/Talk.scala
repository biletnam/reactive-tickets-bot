package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.telegram.actor.TelegramPush
import com.github.bsnisar.tickets.telegram.TelegramMessages
import com.typesafe.scalalogging.LazyLogging

import scala.util.matching.Regex


object Talk {
  def props(charID: String,  stationsSearch: ActorRef, telegram: ActorRef): Props =
    Props(classOf[Talk], charID, stationsSearch, telegram)



  val StationsSearchCommands: Regex = "^(/from|/to)\\s.*".r
  val StationsPointerCommands: Regex = "^(/from_|/to_)\\s.*".r

}

class Talk(val charID: String,
           val stationsSearch: ActorRef,
           val telegram: ActorRef) extends Actor with LazyLogging {

  import com.github.bsnisar.tickets.telegram.TelegramUpdates.Update._

  override def receive: Receive = {
    case update @ Text(Talk.StationsSearchCommands(_*)) =>
      stationsSearch ! update

    case update @ Text(Talk.StationsPointerCommands(_*)) =>

    case send: TelegramMessages.Msg =>
      telegram ! TelegramPush.PushMessage(charID, send)
  }
}
