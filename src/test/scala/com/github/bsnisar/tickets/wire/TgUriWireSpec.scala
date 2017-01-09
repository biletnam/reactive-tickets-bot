package com.github.bsnisar.tickets.wire

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.junit.Assert
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class TgUriWireSpec extends FlatSpec with BeforeAndAfterAll {
  behavior of "A UrlWire"

  implicit val system = ActorSystem()

  it should "append token to request path as head segment" in {
    val token = "ABC100x0"
    val origin = new GatheringMockWire(new MockWire("{}"))
    val wire = new TgUriWire(token, origin)
    val get = RequestBuilding.Get("/getUpdates")
    implicit val mt = ActorMaterializer()

    val result = Source.single(get -> 42).via(wire.flow).runWith(Sink.head)
    Await.ready(result, Duration.Inf)

    Assert.assertEquals(s"/bot$token/getUpdates", origin.requests.get(0)._1.uri.toString())
  }

  it should "append token to request path with slash" in {
    val token = "ABC100--0"
    val origin = new GatheringMockWire(new MockWire("{}"))
    val wire = new TgUriWire(token, origin)
    val get = RequestBuilding.Get("me")
    implicit val mt = ActorMaterializer()

    val result = Source.single(get -> 42).via(wire.flow).runWith(Sink.head)
    val req = Await.result(result, Duration.Inf)

    Assert.assertEquals(s"/bot$token/me", origin.requests.get(0)._1.uri.toString())
  }

  override protected def afterAll(): Unit = system.terminate()
}
