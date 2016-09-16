package org.tickets.api

import akka.actor.Actor
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import net.codingwell.scalaguice.ScalaModule

/**
  * Created by bsnisar on 16.09.16.
  */
class ApiModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[Actor].annotatedWith(Names.named(Telegram.name)).to[Telegram]
  }
}
