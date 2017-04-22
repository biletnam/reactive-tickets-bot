package com.github.bsnisar.tickets.telegram

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.github.bsnisar.tickets.BaseTest
import com.github.bsnisar.tickets.wire.{MockWire, SpyWire}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class RgTelegramSpec extends TestKit(ActorSystem()) with BaseTest {

  implicit val mt  = ActorMaterializer()

  override protected def afterAll(): Unit = {
    shutdown()
  }

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
    val updates: Telegram = new RgTelegram(wire)
    val result: Iterable[TgUpdate] = Await.result(updates.pull(offset), Duration.Inf)

    assert(result.nonEmpty)
    assert(result.head.id === 1001)
    assert(result.head.text === "TestMsg")
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
    val updates: Telegram = new RgTelegram(wire)
    Await.ready(updates.pull(offset), Duration.Inf)

    assert(wire.requests.get(0)._2 == 200)
  }

}
