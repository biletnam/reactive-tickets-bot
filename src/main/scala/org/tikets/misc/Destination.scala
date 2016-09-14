package org.tikets.misc

/**
  * Created by bsnisar on 14.09.16.
  */
sealed trait Destination
case object From extends Destination
case object To extends Destination
