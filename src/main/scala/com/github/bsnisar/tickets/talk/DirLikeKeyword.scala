package com.github.bsnisar.tickets.talk

/**
  * Check command by `contains` operation and delegate.
  * @author bsnisar
  */
class DirLikeKeyword(keyword: String, origin: Directive) extends Directive {
  override def apply(cmd: String): Option[Transit] = {
    if (cmd.contains(keyword)) origin.apply(cmd) else None
  }

  override def talk: Talk = origin.talk
}
