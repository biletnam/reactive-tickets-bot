package com.github.bsnisar.tickets.talk

import scala.concurrent.Future

/**
  * Created by bsnisar on 13.01.17.
  */
case class Transit(talk: Future[Talk], messages: List[String] = List.empty)
