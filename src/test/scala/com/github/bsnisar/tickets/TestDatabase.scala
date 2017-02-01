package com.github.bsnisar.tickets

import slick.jdbc.H2Profile.api._

object TestDatabase {

  lazy val db: Database = {
    val db = Database.forURL(url = "jdbc:h2:mem:test_db", user = "sa")
    db
  }
}
