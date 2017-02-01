package com.github.bsnisar.tickets

import slick.jdbc.H2Profile.api._

object TestDatabase {

  lazy val db: Database = {
    //If you want to keep your content you have to configure the url like this
    //jdbc:h2:mem:test;DB_CLOSE_DELAY=-1
    //If doing so, h2 will keep its content as long as the vm lives.
    val db = Database.forURL(url = "jdbc:h2:mem:test_db;DB_CLOSE_DELAY=-1", user = "sa")
    db
  }
}
