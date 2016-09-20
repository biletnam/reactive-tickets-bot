package org.tickets.bot


trait Question {

}

/**
  * Find stations by given name for departure point for route.
  * @param likeName like name
  */
final case class RoutesFromStation(likeName: String)

/**
  * Find stations by given name for arrival point for route.
  * @param likeName like name
  */
final case class RoutesToStation(likeName: String)

/**
  * Complex question for routes.
  * @param from
  * @param to
  */
case class FindRoutes(from: RoutesFromStation, to: RoutesToStation)