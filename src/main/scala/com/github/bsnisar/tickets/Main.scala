package com.github.bsnisar.tickets

import com.google.inject.{Guice, Stage}


object Main extends App {
  Guice.createInjector(Stage.PRODUCTION, new MainModule)
}
