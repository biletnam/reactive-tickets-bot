package org.tickets.bot.uz

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.scaladsl.Flow
import org.tickets.misc.HttpSupport

import scala.concurrent.Future

class TokenFlow {
  import akka.http.scaladsl.util.FastFuture._

  /**
    * Load token from root page.
    * @return token
    */
  def loadToken: Future[String] =
    rootPage.fast.flatMap(extractPageBody).map(extractToken)


  /**
    * Fetch root page content.
    * @return async http response
    */
  private def rootPage: Future[HttpResponse] =
    Http().singleRequest(RequestBuilding.Get(TokenFlow.UzRootPage))


  /**
    * Extract token from page body.
    * @param pageContent page body as string
    * @return fresh token
    */
  private def extractToken(pageContent: String): String = UzToken(pageContent)


  /**
    * Extract page body as string
    * @param response http response
    * @return page body
    */
  private def extractPageBody(response: HttpResponse): Future[String] = {
    val um: FromEntityUnmarshaller[String] =
      Unmarshaller.byteStringUnmarshaller
        .mapWithCharset { (data, charset) =>
          data.decodeString(charset.nioCharset.name)
        }

    um(response.entity)
  }

}

object TokenFlow {
  val UzRootPage = "/"

  def foo() = {
    Flow.apply[HttpSupport.Request].mapAsyncUnordered[HttpSupport.Request](100) { req =>
      ???
    }
  }
}
