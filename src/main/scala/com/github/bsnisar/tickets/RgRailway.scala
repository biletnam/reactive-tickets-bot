package com.github.bsnisar.tickets

import akka.stream.Materializer
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.wire.Wire
import org.json4s.JValue

/**
  * Http {akka http} railway api.
  *
  * @author bsnisar
  */
class RgRailway(private val wire: Wire[Req, JValue])
               (implicit
                val mt: Materializer) extends Railway {


  override def trains: Trains = ???

  override def stations: Stations = new StationsUz(wire)
}
