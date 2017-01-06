package com.github.bsnisar.tickets

import akka.stream.scaladsl.Flow

/**
  * Wire.
  */
trait Wire[-In, +Out] {

  /**
    * Build flow for transforming input to output.
    *
    * @return flow.
    */
  def flow: Flow[In, Out, _]

}
