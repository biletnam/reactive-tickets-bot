package com.github.bsnisar.tickets.talk

import scala.concurrent.Future

/**
  * Created by bsnisar on 12.01.17.
  */
trait Talk {

  /**
    * Available transitions from particular dialog position.
    */
  def forks: List[String => Option[Transit]]

  /**
    * Talk context.
    */
  def memory: Map[String, Any]

  /**
    * Last message, related to this talk.
    */
  def msg: String = ""
}



