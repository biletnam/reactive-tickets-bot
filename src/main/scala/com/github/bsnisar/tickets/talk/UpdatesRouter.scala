package com.github.bsnisar.tickets.talk

import akka.actor.{Actor, ActorRef, Props}
import com.github.bsnisar.tickets.talk.UpdatesProducer.Continue
import com.github.bsnisar.tickets.telegram.Update
import com.typesafe.scalalogging.LazyLogging

object UpdatesRouter {
  def props(routes: List[RouteLogic[Update]]): Props = Props(classOf[UpdatesRouter], routes)
  def props(routes: RouteLogic[Update]*): Props = Props(classOf[UpdatesRouter], routes.toList)
}

class UpdatesRouter(routes: List[RouteLogic[Update]]) extends Actor with LazyLogging {


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

trait RouteLogic[A] {
  final type Routee = PartialFunction[Update, RouteEvent[A]]

  val specify: PartialFunction[Update, RouteEvent[A]]
  def ref: ActorRef
  def send(event: RouteEvent[A]): Unit = ref ! event
}