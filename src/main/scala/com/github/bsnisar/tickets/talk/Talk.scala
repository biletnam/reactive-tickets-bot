package com.github.bsnisar.tickets.talk

trait Talk {

  def act(cmd: String): Either[Talk, Any]

}
