package org.tikets

import akka.actor.Actor

/**
  * Created by bsnisar on 14.09.16.
  */
class Telegram extends Actor {
  override def receive: Receive = ???
}

object Telegram {

  final case class SelectStation(names: List[String])

}
