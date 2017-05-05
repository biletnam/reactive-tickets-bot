package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.telegram.Update
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec


object TalksRoutee {
  def props(routes: List[RouteLogic[Update]]): Props = Props(classOf[TalksRoutee], routes)
  def props(routes: RouteLogic[Update]*): Props = Props(classOf[TalksRoutee], routes.toList)
}

class TalksRoutee(routes: List[RouteLogic[Update]]) extends Actor with LazyLogging {

  override def receive: Receive = {
    case update: Update =>
      val maybeRoute = routes.find(_.specify.isDefinedAt(update))
      if (maybeRoute.isDefined) {
        val logic = maybeRoute.get
        val event = logic.specify(update)
        logger.debug(s"deliver by route $logic")
        logic.send(event)
      } else {
        logger.error(s"route not found for $update")
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
  val specify: PartialFunction[Update, RouteEvent[A]]
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