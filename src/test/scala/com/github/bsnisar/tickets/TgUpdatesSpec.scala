package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.github.bsnisar.tickets.wire.{GatheringMockWire, MockWire}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, BeforeAndAfterAll, FlatSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class TgUpdatesSpec extends TestKit(ActorSystem()) with BaseTest {

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

    val wire = new MockWire(json)
    val updates: Updates = new TgUpdates(wire)
    val result: Iterable[Update] = Await.result(updates.pull, Duration.Inf)

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

    val wire = new GatheringMockWire(new MockWire(json))
    val updates: Updates = new TgUpdates(wire)
    updates.pull
    updates.pull
  }

}
