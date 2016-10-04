package org.tickets.bot.tg


object TelegramRequests {

  /**
    *
    * @param name
    */
  case class FindRouteFrom(name: String)

  /**
    *
    * @param name
    */
  case class FindRouteTo(name: String)

  case object NeedDestination

}
