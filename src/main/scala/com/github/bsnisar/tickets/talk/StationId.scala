package com.github.bsnisar.tickets.talk


import java.util.Base64

import com.google.common.base.Charsets

import scala.util.Try
import scala.util.matching.Regex

object StationId {
  val Prefix = "/goto_"
  val FromKey = "f::"
  val ToKey = "t::"

  val PrefixPattern: Regex = "^/goto_(.*?)".r
  val FromKeyPattern: Regex = "^f::(.*?)".r
  val ToKeyPattern: Regex = "^t::(.*?)".r
}

/**
  * Encoder and decoder, that store mnemonic in as telegram command.
  */
trait StationId {
  def encode(id: String, from: Boolean): String
  def decode(id: String): Try[Id]

  case class Id(id: String, from: Boolean)
}

/**
  * Base64 encoder.
  */
class StationIdBase64(cmdPrefix: String = "/goto_") extends StationId {
  override def encode(id: String, isDeparture: Boolean): String = {
    val str = s"${if (isDeparture) StationId.FromKey else StationId.ToKey}$id"
    val encoded = Base64.getEncoder.encodeToString(str.getBytes(Charsets.UTF_8))
    s"$cmdPrefix$encoded"
  }

  override def decode(id: String): Try[Id] = Try {
    id match {
      case StationId.PrefixPattern(data) =>
        val raw = Base64.getDecoder.decode(data)
        val decoded = new String(raw, Charsets.UTF_8)
        decoded match {
          case StationId.ToKeyPattern(realID) => Id(realID, from = false)
          case StationId.FromKeyPattern(realID) => Id(realID, from = true)
          case _ => throw new IllegalArgumentException(s"unexpected encoded id format [$decoded]")
        }

      case _ => throw new IllegalArgumentException(s"unexpected id format [$id]")
    }
  }
}
