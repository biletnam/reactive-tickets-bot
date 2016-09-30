package org.tickets.bot.uz

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import akka.stream.scaladsl.Flow
import org.tickets.misc.HttpSupport

import scala.concurrent.{ExecutionContext, Future}

class TokenFlow {
  import akka.http.scaladsl.util.FastFuture._

  /**
    * Load token from root page.
    * @return token
    */
  def loadToken(implicit ec: ExecutionContext, mt: Materializer, as: ActorSystem): Future[String] =
    rootPage.fast.flatMap(extractPageBody).map(extractToken)


  /**
    * Fetch root page content.
    * @return async http response
    */
  private def rootPage(implicit as: ActorSystem, mt: Materializer): Future[HttpResponse] =
    Http().singleRequest(RequestBuilding.Get(UzApi.RootPage))


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
  private def extractPageBody(response: HttpResponse)(implicit ex: ExecutionContext, mt: Materializer): Future[String] = {
    val um: FromEntityUnmarshaller[String] =
      Unmarshaller.byteStringUnmarshaller
        .mapWithCharset { (data, charset) =>
          data.decodeString(charset.nioCharset.name)
        }

    um(response.entity)
  }

}

object UzApi {
  val RootPage = "http://booking.uz.gov.ua"

  val FIND_STATIONS_URL = "/purchase/station/{stationNameFirstLetters}/"
  val FIND_TRAINS_URL = "/purchase/search/"
  val GET_COACHES_URL = "/purchase/coaches/"
  val GET_FREE_SEATS_URL = "/purchase/coach/"


  def foo() = {
    Flow.apply[HttpSupport.Request].mapAsyncUnordered[HttpSupport.Request](100) { req =>
      ???
    }
  }
}
