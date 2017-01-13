package com.github.bsnisar.tickets.talk

import com.github.bsnisar.tickets.{Station, Stations}

import scala.concurrent.Future

class DirStations(val stations: Stations, val talk: Talk) extends Directive {

  override def apply(cmd: String): Option[Transit] = {
    val f: Future[Map[String, Station]] = stations.stationsByName(cmd)
        .map { sts =>
          val stringToStation: Map[String, Station] = sts.view
            .groupBy(s => s.id)
            .map { case (id, collect) => id -> collect.head }

          stringToStation
        }

//    f.map()

    val result = f.recover {
      case (ex) => ???
      case _ => ???
    }

    ???
  }


}
