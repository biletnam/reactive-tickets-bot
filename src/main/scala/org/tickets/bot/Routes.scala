package org.tickets.bot


import akka.actor.{ActorRef, ActorSelection}
import akka.persistence.PersistentActor
import org.tickets.misc.Log
import org.tickets.msg.RouteQuery


class Routes(val persistenceId: String, val telegram: ActorRef) extends PersistentActor with Log {
  private val stations: ActorSelection = context.actorSelection("/stations")
  private val tickets: ActorSelection = context.actorSelection("/tickets")

  override def receiveCommand: Receive = {
    case RouteQuery(from, to, arriveAt) =>

  }

  override def receiveRecover: Receive = ???
}

object Routes {

  /**
    * Root event that modify request.
    */
  sealed trait Req

}