package org.tikets.msg

/**
  * Bot input as phrase with specific keywords:
  * {{{
  *   /hello some_value
  * }}}
  *
  * @author bsnisar
  */
trait Phrase {

  /**
    * Command keyword
    * @return keyword
    */
  def command: String
}
