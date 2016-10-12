package org.tickets.chat

import akka.NotUsed
import akka.actor.Actor
import akka.actor.Actor.Receive
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpRequest
import akka.stream._
import akka.stream.actor.ActorPublisher
import akka.stream.scaladsl.{Flow, GraphDSL, Merge, RunnableGraph, Sink, Source}
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import com.google.common.base.{Preconditions, Ticker}
import org.tickets.chat.Pull.{Ack, Offset, Pull, PullAgain, Tick}
import org.tickets.chat.Telegram.{BotToken, TgMethod, TgReq, TgRes}

import scala.concurrent.duration._

object Pull {

  type Tick = PullEvent
  type Pull = PullReq

  /**
    * Pull events for given offset.
    */
  trait PullReq {
    def offset: Int
  }

  /**
    * Pull with offset.
    * @param offset some offset
    */
  case class Offset(offset: Int) extends PullReq

  /**
    * Pull with unknown offset.
    */
  class UnknownOffset extends PullReq {
    override def offset: Int = -1
  }

  /**
    * Type of event.
    */
  trait PullEvent

  /**
    * Scheduled tick for updates.
    */
  case object Tick extends PullEvent

  /**
    * Pull consumed with max update id
    * @param maxUpdate max update id
    */
  case class Ack(maxUpdate: Int) extends PullEvent

  /**
    * Something goose wrong. Pull same updates again.
    */
  case object PullAgain extends PullEvent
}

/**
  * Generate Pull requests with sequence, in a cursor way.
  */
final class CursorPuller(awaitTime: FiniteDuration = 10.seconds) extends GraphStage[FlowShape[Tick, Pull]] {
  val in = Inlet[Tick]("CursorPuller.in")
  val out = Outlet[Pull]("CursorPuller.out")

  override val shape = FlowShape.of(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) {

    private var currentValue: Tick = _
    private var lastUpdateSeq = -1
    private var lastPull = Ticker.systemTicker().read()
    private var freshSeq = true

    private def elapsed(): Boolean = {
      val currentTick = Ticker.systemTicker().read()
      currentTick - lastPull > awaitTime.toNanos
    }

    private def refreshState(seq: Int): Unit = {
      lastUpdateSeq = seq
      lastPull = Ticker.systemTicker().read()
      freshSeq = true
    }

    setHandlers(in, out, new InHandler with OutHandler {
      override def onPush(): Unit = {
        currentValue = grab(in)

        currentValue match {
          case Tick if freshSeq =>
            freshSeq = false
            push(out, new Pull.UnknownOffset)

          // pull req was acknowledged
          case Ack(id) if id > lastUpdateSeq =>
            refreshState(id)
            if (isAvailable(out)) {
              push(out, Offset(lastUpdateSeq))
            }

          case Ack(id) =>
            println("ask with id higher was pull previously")
        }

        pull(in)
      }

      override def onPull(): Unit = {
        if (!freshSeq) {
          freshSeq = false
          push(out, Offset(lastUpdateSeq))
        }

      }
    })

    override def preStart(): Unit = {
      pull(in)
    }
  }
}


/*
class Pull(botToken: BotToken) extends ActorPublisher[TgReq] with Json4sSupport {
  override def receive: Receive = ???

  private val flow: Flow[TgReq, TgRes, Any] = _

  private var maxUpdateId = -1

  def pulling(): Receive = {
    case Tick => sendReq()
    case Ack(id) =>
  }


  def sendReq(): Unit = {
    if (isActive && totalDemand > 0) {
      onNext(nextPullReq())
    }
  }

  def nextPullReq(): TgReq = {
    val req = if (maxUpdateId >= 0)
      RequestBuilding.Post(botToken.GetUpdatesUri, Map("offset" -> maxUpdateId))
    else
      RequestBuilding.Post(botToken.GetUpdatesUri)

    req -> GetUpdates
  }
}
*/
