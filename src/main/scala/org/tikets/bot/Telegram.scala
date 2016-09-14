package org.tikets.bot

import akka.actor.Actor
import akka.actor.Actor.Receive


object Telegram {


  final case class AskSelectStation(names: List[String])



}

/**
  * Created by bsnisar on 14.09.16.
  */
class Telegram extends Actor {
  override def receive: Receive = ???
}
