package com.github.bsnisar.tickets

import java.util.Locale

import com.typesafe.scalalogging.LazyLogging
import slick.jdbc.H2Profile.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Stations. Looks in database and then fetch stations from original Stations object.
  * Information, that was gotten from origin [[Stations]] will be persisted in the database.
  * @param origin original Stations
  * @param db database
  */
class StationsDb(private val origin: Stations, private val db: Database) extends Stations with LazyLogging {
  import StationsDb.{StationTranslations, Stations}

  /**
    * @inheritdoc
    *
    * Try to find from database first. And add not exist results into it.
    *
    * @param name name like
    * @return list of stations
    */
  override def stationsByName(name: String): Future[Iterable[Station]] = {
    val stationsQuery = for {
      (s, ts) <- Stations join StationTranslations on (_.id === _.stationID)
               if (ts.local === "en") && (ts.l19nName like s"%$name%")
    } yield (s.apiID, ts.l19nName)

    val records = db.run(stationsQuery.result)
    records.flatMap {
      case Seq() =>
        logger.debug(s"#stationsByName - call origin.stationsByName($name)")
        val result = for {
          resp <- origin.stationsByName(name)
          _ <- persist(resp)
        } yield resp

        result
      case foundRows =>
        Future.successful(foundRows.map {
          case (apiId, l19nName) => Station(apiId, l19nName)
        })
    }
  }

  private def persist(stations: Iterable[Station]) = {
    def insertStationIfAbsent(apiID: String, local: String, l19nName: String) = {
      val stationInsQuery = Stations returning Stations.map(_.id) forceInsertQuery {
        val exists = (for {q <- Stations if q.apiID === apiID} yield q).exists
        val stationInsert: (Option[Long], String) = (None, apiID)
        for {q <- Query(stationInsert) if !exists} yield q
      }

      stationInsQuery.flatMap { generatedId =>
        val stationID = generatedId.head
        StationTranslations.forceInsertQuery {
          val exists = (for {q <- StationTranslations if q.stationID === stationID} yield q).exists
          val translationInsert: (Long, String, String) = (stationID, local, l19nName)
          for {q <- Query(translationInsert) if !exists} yield q
        }
      }
    }

    val actions = DBIO.sequence(for {
      station <- stations
    } yield insertStationIfAbsent(station.id, "en", station.name))

    db.run(actions)
  }
}

// scalastyle:off public.methods.have.type
object StationsDb {
  class StationsTable(tag: Tag) extends Table[(Option[Long], String)](tag, "STATION") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)
    def apiID = column[String]("API_ID")
    override def * = (id.?, apiID)
  }
  val Stations = TableQuery[StationsTable]

  class StationTranslationsTable(tag: Tag) extends Table[(Long, String, String)](tag, "TRANSLATION_STATION") {
    def stationID = column[Long]("STATION_ID", O.PrimaryKey)
    def local = column[String]("LOCAL_CODE")
    def l19nName = column[String]("NAME")
    def station = foreignKey("TRANSL_STATION_REF_STATION", stationID, Stations)(_.id,
      onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade
    )
    override def * = (stationID, local, l19nName)
  }

  val StationTranslations = TableQuery[StationTranslationsTable]
}
