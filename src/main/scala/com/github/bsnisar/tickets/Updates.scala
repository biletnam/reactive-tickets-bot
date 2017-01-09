package com.github.bsnisar.tickets

import scala.concurrent.Future

trait Updates {

  /**
    * Pull for updates.
    *
    * @return updates.
    */
  def pull: Future[Iterable[Update]]

}
