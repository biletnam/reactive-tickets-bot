package org.tikets.bot

import akka.actor.Actor
import akka.actor.Actor.Receive


object Client {


  final case class AskSelectStation(names: List[String])



}

/**
  * Created by bsnisar on 14.09.16.
  */
class Client extends Actor {
  override def receive: Receive = ???
}
