package com.github.bsnisar.tickets
import akka.stream.Materializer
import com.github.bsnisar.tickets.Ws.Req
import com.github.bsnisar.tickets.misc.Json
import com.github.bsnisar.tickets.wire.Wire
import org.json4s._

/**
  * Http based telegram API.
  */
class RgTelegram(val wire: Wire[Req, JValue])
                (implicit
                 val mt: Materializer) extends Telegram with Json {

  override def push(chatId: Long, msg: String): Unit = ???

  override def updates: Updates = ???
}
