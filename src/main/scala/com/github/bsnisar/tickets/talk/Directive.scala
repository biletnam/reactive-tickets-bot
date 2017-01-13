package com.github.bsnisar.tickets.talk

/**
  * Created by bsnisar on 13.01.17.
  */
trait Directive extends (String => Option[Transit]) {

  def talk: Talk
}
