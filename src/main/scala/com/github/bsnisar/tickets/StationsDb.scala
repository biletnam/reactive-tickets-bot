package com.github.bsnisar.tickets

import scala.concurrent.Future

import slick.driver.H2Driver.api._


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
    ???
  }

  private def findIdsLocalizedName(name: String) = {
    for {
      (state, l19n) <- Stations join StationTranslations if l19n.l19nName like name
    } yield state
  }


  private def addTranslationIfNotExists(id: Long, local: String, nameValue: String) =
    StationsDb.StationTranslations.forceInsertQuery {
      val exists = (for {
        str <- StationsDb.StationTranslations if str.id == id.bind
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
    def id = column[Long]("station_id", O.PrimaryKey)
    def local = column[String]("local_code")
    def l19nName = column[String]("name")
    def station = foreignKey("transl_stations_ref_stations", id, Stations)(_.id, onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    override def * = (id, local, l19nName)
  }

  val StationTranslations = TableQuery[StationTranslationsTable]
}
