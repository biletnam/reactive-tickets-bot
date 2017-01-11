package com.github.bsnisar.tickets.talk


case class TkForks(forks: Map[String, Talk], tail: Talk) extends Talk {
  override def act(cmd: String): Either[Talk, Any] = {
    forks.get(cmd) match {
      case Some(tk) => ???
      case None => Right("")
    }
  }
}