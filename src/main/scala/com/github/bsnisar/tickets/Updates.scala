package com.github.bsnisar.tickets

import scala.concurrent.Future

/**
  * Telegram Bot API updates.
  *
  * @author bsnisar
  */
trait Updates {

  /**
    * Pull for updates.
    *
    * @return updates.
    */
  def pull: Future[Iterable[Update]]

}
