package org.tickets.misc

import java.util.regex.Pattern

/**
  * Telegram command that contains some mnemonic.
  */
trait IdLikeCommand[T] {

  /**
    * Produce this command
    * @param t element
    * @return string
    */
  def encode(t: T): String

  /**
    * Get mnemonic value from id.
    * @param id id
    * @return parsed type
    */
  def decode(id: String): T
}

class PrefixedIdLike[T >: String](val prefix: String) extends IdLikeCommand[T] {
  private lazy val pattern = Pattern.compile(prefix)

  override def encode(t: T): String =
    s"$prefix$t"
  override def decode(id: String): T =
    pattern.matcher(id).replaceAll("")
}