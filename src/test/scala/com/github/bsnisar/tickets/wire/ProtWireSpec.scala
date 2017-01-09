package com.github.bsnisar.tickets.wire

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import akka.testkit.TestKit
import org.json4s.JValue
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{Assertions, BeforeAndAfterAll, FlatSpecLike}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.Failure

@RunWith(classOf[JUnitRunner])
class ProtWireSpec extends TestKit(ActorSystem()) with FlatSpecLike with BeforeAndAfterAll with Assertions {

  implicit val mt  = ActorMaterializer()

  override protected def afterAll(): Unit = {
    shutdown()
  }

  "A ProtWire" should "proceed exceptions" in {
    val wire = new ProtWire(new MockWire("{}"), (json: JValue) =>
      Failure(new IllegalStateException())
    )

    val get = RequestBuilding.Get("/get")
    val response = Source.single(get -> 42).via(wire.flow).runWith(Sink.head)

    assertThrows[IllegalStateException] {
      Await.result(response, Duration.Inf)
    }
  }

  it should "proceed exception in map" in {
    val wire = new MockWire("{}")

    val get = RequestBuilding.Get("/get")
    val response = Source.single(get -> 42).via(wire.flow)
      .map(e => throw new IllegalStateException)
      .runWith(Sink.head)

    assertThrows[IllegalStateException] {
      Await.result(response, Duration.Inf)
    }
  }

}
