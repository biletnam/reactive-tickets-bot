package org.tickets.telegram

import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.persistence.PersistentActor

object Publisher {
  case object Tick
}

class Publisher extends Actor {
  private var seq: Int = _



  override def receive: Receive = ???
}
