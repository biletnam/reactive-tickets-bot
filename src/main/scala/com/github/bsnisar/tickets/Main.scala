package com.github.bsnisar.tickets

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Main extends App {
  implicit val as = ActorSystem()
  implicit val am = ActorMaterializer()



}
