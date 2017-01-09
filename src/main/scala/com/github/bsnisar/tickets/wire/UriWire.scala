package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.Uri.Path
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.{Req, Task}

/**
  * Prepend token to head segment:
  *
  * {{{
  *   "/getMe" -> "/bot${botToken}/getMe"
  * }}}
  *
  * @author bsnisar
  */
class UriWire(private val token: String) extends Wire[Req, Req] {
  import Path._

  private lazy val suffix = / (s"bot$token")

  override def flow: Flow[(HttpRequest, Task), Req, _] =
    Flow[Req].map {
      case (request, task) =>
        val path = request.uri.path
        val newPath = if (path.startsWithSlash) {
          suffix ++ path
        } else {
          suffix ++ / ++ path
        }

        val newUri = request.uri.withPath(newPath)
        request.withUri(newUri) -> task
    }
}
