package org.tickets.misc

import com.onyx.persistence.factory.impl.{CacheManagerFactory, EmbeddedPersistenceManagerFactory}

/**
  * Created by bsnisar on 15.10.16.
  */
class OnyxPersistenceProvider {

  val factory = new CacheManagerFactory()
  factory.setCredentials("username", "password")
  val pathToDB = ""
//  factory.setDatabaseLocation(pathToDB)
  factory.initialize()
  factory.getPersistenceManager()

}
