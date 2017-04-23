package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
abstract class AkkaBaseTest(as: ActorSystem) extends TestKit(as) with BaseTest {
  override protected def afterAll(): Unit = shutdown()
}
