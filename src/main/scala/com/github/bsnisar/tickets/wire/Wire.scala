package com.github.bsnisar.tickets.wire

import akka.stream.scaladsl.Flow

/**
  * Wire.
  *
  * @author bsnisar
  */
trait Wire[-In, +Out] {

  /**
    * Build flow for transforming input to output.
    *
    * @return flow.
    */
  def flow: Flow[In, Out, _]

}
