package com.github.bsnisar.tickets

import scala.concurrent.Future
import scala.xml.NodeSeq

/**
  * Trains.
  *
  * @author bsnisar
  */
trait Trains {

  def find(query: NodeSeq): Future[Iterable[Train]]

}
