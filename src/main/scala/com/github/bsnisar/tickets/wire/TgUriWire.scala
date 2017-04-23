package com.github.bsnisar.tickets.wire

import akka.http.scaladsl.model.Uri.Path
import akka.stream.scaladsl.Flow
import com.github.bsnisar.tickets.Ws.Req
import com.google.common.base.Strings

/**
  * Add special prefix to URI:
  *
  * {{{
  *   "/getMe" -> "/bot${botToken}/getMe"
  * }}}
  *
  * @author bsnisar
  */
class TgUriWire[A](private val token: String, origin: Wire[Req, A]) extends Wire[Req, A] {
  import Path._

  require(!Strings.isNullOrEmpty(token), "isNullOrEmpty(token)")

  private lazy val suffix = / (s"bot$token")

  override def flow: Flow[Req, A, _] =
    Flow[Req].map {
      case (request, param) =>
        val path = request.uri.path
        val newPath = if (path.startsWithSlash) {
          suffix ++ path
        } else {
          suffix ++ / ++ path
        }

        val newUri = request.uri.withPath(newPath)
        request.withUri(newUri) -> param
    }.via(origin.flow)
}
