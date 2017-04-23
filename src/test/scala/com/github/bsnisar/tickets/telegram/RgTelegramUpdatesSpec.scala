package com.github.bsnisar.tickets.telegram

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.bsnisar.tickets.AkkaBaseTest
import com.github.bsnisar.tickets.wire.{MockWire, SpyWire}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

@RunWith(classOf[JUnitRunner])
class RgTelegramUpdatesSpec extends AkkaBaseTest(ActorSystem()) {

  implicit val mt  = ActorMaterializer()

  "A TgUpdates" should "poll response as Updates" in {
    val json =
      """
        |[
        |  {
        |  "update_id": 1001,
        |  "message": {
        |        "message_id": 1,
        |        "text": "TestMsg"
        |     }
        |  }
        |]
      """.stripMargin

    val offset = 1001
    val wire = new MockWire(json)
    val updates = new RgTelegramUpdates(wire)
    val result = Await.result(updates.pull(offset), Duration.Inf)

    assert(result.data.nonEmpty)
    assert(result.data.head.seqNum === 1001)
    assert(result.data.head.text === "TestMsg")
  }

  it should "ask next messages with increased offset" in {
    val json =
      """
        |[
        |  {
        |    "update_id": 1001,
        |    "message": {
        |        "message_id": 1,
        |        "text": "TestMsg1"
        |    }
        |  },
        |  {
        |    "update_id": 2000,
        |    "message": {
        |        "message_id": 2,
        |        "text": "TestMsg2"
        |    }
        |  }
        |]
      """.stripMargin

    val offset = 200
    val wire = new SpyWire(new MockWire(json))
    val updates = new RgTelegramUpdates(wire)
    Await.ready(updates.pull(offset), Duration.Inf)

    assert(wire.requests.get(0)._2 == 200)
  }

}
