package com.github.bsnisar.tickets

import java.net.{URLDecoder, URLEncoder}

import com.google.common.base.Charsets


object Main extends App {
//  Guice.createInjector(Stage.PRODUCTION, new MainModule)
  val encName = URLEncoder.encode("Днепр", Charsets.UTF_8.name())
  val decName = URLDecoder.decode("%2525D0%252594%2525D0%2525BD%2525D0%2525B5%2525D0%2525BF%2525D1%252580", Charsets.UTF_8.name())
  println(encName)
  println(decName)
}

