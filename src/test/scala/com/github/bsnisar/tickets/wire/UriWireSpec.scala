package com.github.bsnisar.tickets.wire

import akka.actor.ActorSystem
import akka.http.scaladsl.client.RequestBuilding
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import com.github.bsnisar.tickets.Ws.Req
import org.junit.Assert
import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, FlatSpec}
import org.scalatest.junit.JUnitRunner

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@RunWith(classOf[JUnitRunner])
class UriWireSpec extends FlatSpec with BeforeAndAfterAll {
  behavior of "An UrlWire"

  implicit val system = ActorSystem()

  it should "append token to request path as head segment" in {
    val token = "ABC100x0"
    val wire = new UriWire(token)
    val get = RequestBuilding.Get("/getUpdates")
    implicit val mt = ActorMaterializer()

    val result: Future[Req] = Source.single(get -> 42).via(wire.flow).runWith(Sink.head)
    val req = Await.result(result, Duration.Inf)

    Assert.assertEquals(s"/bot$token/getUpdates", req._1.uri.toString())
  }

  it should "append token to request path with slash" in {
    val token = "ABC100--0"
    val wire = new UriWire(token)
    val get = RequestBuilding.Get("me")
    implicit val mt = ActorMaterializer()

    val result: Future[Req] = Source.single(get -> 42).via(wire.flow).runWith(Sink.head)
    val req = Await.result(result, Duration.Inf)

    Assert.assertEquals(s"/bot$token/me", req._1.uri.toString())
  }

  override protected def afterAll(): Unit = system.terminate()
}
