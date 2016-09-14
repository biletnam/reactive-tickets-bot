package org.tikets.uz

/**
  * Created by bsnisar on 14.09.16.
  */
trait Stations {

  def findMatches(name: String): Iterable[Station]
}
