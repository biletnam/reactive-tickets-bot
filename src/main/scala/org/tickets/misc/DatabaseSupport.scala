package org.tickets.misc

import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import liquibase.{Contexts, Liquibase}
import slick.driver.H2Driver
import slick.driver.H2Driver.api._

import scala.language.reflectiveCalls


object DatabaseSupport {
  type DB = H2Driver.backend.Database

  def loadDatabase: DB = {
    val db: DB = Database.forConfig("h2db")

    using(db.source.createConnection()) { conn =>
      val liquibase = new Liquibase("db/changelog.xml",
        new ClassLoaderResourceAccessor,
        new JdbcConnection(conn))

      liquibase.update(new Contexts)
    }

    db
  }

  private def using[X <: {def close()}, A](resource : X)(f : X => A) = {
    try {
      f(resource)
    } finally {
      resource.close()
    }
  }
}
