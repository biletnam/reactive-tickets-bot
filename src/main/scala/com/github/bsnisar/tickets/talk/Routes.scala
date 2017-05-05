package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.telegram.Update
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec


object Routes {
  def props(routes: List[RouteLogic[Update]]): Props = Props(classOf[Routes], routes)
  def props(routes: RouteLogic[Update]*): Props = Props(classOf[Routes], routes.toList)
}

class Routes(routes: List[RouteLogic[Update]]) extends Actor with LazyLogging {


  override def receive: Receive = {
    case update: Update =>
      try {
        deliver(update, routes.head, routes.tail)
      }
      catch {
        case ex: Throwable => logger.error(s"failed to deliver update", ex)
      }
  }


  @tailrec
  private def deliver(update: Update, logic: RouteLogic[Update], routes: List[RouteLogic[Update]]): Unit = {
    val specificEvent = logic.specify
    if (specificEvent.isDefinedAt(update)) {
      logger.debug(s"deliver by route $logic")
      logic.send(specificEvent(update))
    } else if (routes.nonEmpty) {
      deliver(update, routes.head, routes.tail)
    } else {
      throw new IllegalStateException("route not found for " + update)
    }
  }
}

/**
  * Event, wrap all message by routers logic.
  */
trait RouteEvent[A] {
  def update: A
  def payload: Option[Any]
}

case class UpdateEvent(update: Update, payload: Option[Any]) extends RouteEvent[Update]

object UpdateEvent {
  def apply(update: Update, payload: Any): UpdateEvent = UpdateEvent(update, Option(payload))
  def apply(update: Update): UpdateEvent = UpdateEvent(update, None)
}

/**
  * Logic for picking right dessication.
  */
trait RouteLogic[A] {
  def specify: PartialFunction[Update, RouteEvent[A]]
  def send(event: RouteEvent[A]): Unit
}

/**
  * Decorator that send event by specified actor ref.
  */
trait RefRouteLogic[A] extends RouteLogic[A] {
  val specify: PartialFunction[Update, RouteEvent[A]]
  def ref: ActorRef
  override def send(event: RouteEvent[A]): Unit = ref ! event
}