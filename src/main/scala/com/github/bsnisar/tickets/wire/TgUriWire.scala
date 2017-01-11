package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model.Uri.Path
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req

/**
  * Prepend token to head segment:
  *
  * {{{
  *   "/getMe" -> "/bot${botToken}/getMe"
  * }}}
  *
  * @author bsnisar
  */
class TgUriWire[A](private val token: String, origin: Wire[Req, A]) extends Wire[Req, A] {
  import Path._

  private lazy val suffix = / (s"bot$token")

  override def flow: Flow[Req, A, _] =
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
    }.via(origin.flow)
}
