package com.github.bsnisar.tickets

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration.Duration


class StationsDb(private val origin: Stations, private val db: Database) extends Stations {
  import StationsDb.Stations
  import StationsDb.StationTranslations

  /**
    * @inheritdoc
    *
    * Try to find from database first. And add not exist results into it.
    *
    * @param name name like
    * @return list of stations
    */
  override def stationsByName(name: String): Future[Iterable[Station]] = {
    val persistedStations = db.run(findIdsLocalizedName(name).result)
    persistedStations.flatMap {
      case found if found.isEmpty =>
        origin.stationsByName(name)
          .map { foundStations =>
            val inserts = DBIO.sequence(
              foundStations.map(station => updateStationsData(station.id, "en", station.name))
            )

            Await.ready(db.run(inserts), Duration.Inf)
            foundStations
          }
      case found =>
        val stations: Seq[Station] = found.map {
          case ((_, apiID), stationName) => ConsStation(apiID, stationName)
        }

        Future.successful(stations)
    }
  }

  private def updateStationsData(apiID: String, local: String, name: String) =
    addIfAbsentStation(apiID).map(id => addIfAbsentTranslation(id, local, name))

  private def findIdsLocalizedName(name: String) = {
    for {
      (state, l19n) <- Stations join StationTranslations on (_.id === _.stationID)
          if (l19n.l19nName like name) && (l19n.local === "en")
    } yield (state, l19n.l19nName)
  }

  private def addIfAbsentStation(apiID: String) =
    Stations.forceInsertQuery {
      val exists = (for {
        str <- Stations if str.apiUID === apiID
      } yield str).exists
      val insert: (Long, String) = (1, apiID)
      for (qu <- Query(insert) if !exists) yield qu
    }

  private def addIfAbsentTranslation(id: Long, local: String, nameValue: String) =
    StationTranslations.forceInsertQuery {
      val exists = (for {
        str <- StationsDb.StationTranslations if str.stationID == id.bind
      } yield str).exists
      val insert = (id, local, nameValue)
      for (qu <- Query(insert) if !exists) yield qu
    }
}

// scalastyle:off public.methods.have.type
object StationsDb {
  class StationsTable(tag: Tag) extends Table[(Long, String)](tag, "stations") {
    def id = column[Long]("station_id", O.PrimaryKey, O.AutoInc)
    def apiUID = column[String]("api_uid")
    override def * = (id, apiUID)
  }
  val Stations = TableQuery[StationsTable]

  class StationTranslationsTable(tag: Tag) extends Table[(Long, String, String)](tag, "translation_stations") {
    def stationID = column[Long]("station_id", O.PrimaryKey)
    def local = column[String]("local_code")
    def l19nName = column[String]("name")
    def station = foreignKey("transl_stations_ref_stations", stationID, Stations)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )
    override def * = (stationID, local, l19nName)
  }

  val StationTranslations = TableQuery[StationTranslationsTable]
}
