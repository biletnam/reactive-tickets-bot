package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.google.common.base.Supplier
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

/**
  * Created by Bogdan_Snisar on 9/30/2016.
  */
class UzTokenSpec extends FlatSpec with BeforeAndAfterAll with Matchers {

  implicit var system: ActorSystem = null

  override protected def beforeAll(): Unit = {
    system = ActorSystem("sys")
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A UzToken " should " be populated by token" in {
    implicit val mt = ActorMaterializer()
    implicit val ec = system.dispatcher

    val supplier: Supplier[String] = UzToken.singleton
    val res1 = supplier.get()
    val res2 = supplier.get()
    val res3 = supplier.get()
    println(res1)
    res1 should not be null
    res1 should be (res1)
    res1 should be (res2)
    res1 should be (res3)
  }


}
