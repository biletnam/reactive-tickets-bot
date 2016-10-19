package org.tickets.bot

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import akka.persistence.PersistentActor
import org.tickets.bot.TicketsWatcher.SearchReq
import org.tickets.model.TicketsCriteria

object TicketsWatcher {

  /**
    * Find tickets.
    * @param user user who need tickets
    * @param criteria criteria for search
    */
  case class SearchReq(user: ActorRef, criteria: TicketsCriteria)
}

class TicketsWatcher extends PersistentActor {
  override def persistenceId: String = "tickets-watcher"

  override def receiveCommand: Receive = {
    case SearchReq(user, criteria) => ???
  }

  override def receiveRecover: Receive = ???
}
