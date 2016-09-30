package org.tickets.bot.uz

import java.util.regex.Pattern

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.stream.Materializer
import com.google.common.base.{Supplier, Suppliers}
import org.tickets.api.token.JJEncoder

import scala.concurrent.{Await, ExecutionContext, Future}

trait UzToken {
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
  private def extractToken(pageContent: String): String = {
    val matcher = UzToken.EncodedDataPattern.matcher(pageContent)
    require(matcher.find(), "Encoded block not found")

    val encodedTokenData: String = pageContent.substring(matcher.start, matcher.end)
    val decodedTokenData: String = new JJEncoder().decode(encodedTokenData)
    val tokenMatcher = UzToken.TokenPattern.matcher(decodedTokenData)
    require(tokenMatcher.find(), "Token not found")

    val token: String = decodedTokenData.substring(tokenMatcher.start, tokenMatcher.end)
    token
  }


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

/**
  * Extract token from page content.
  * @author Bogdan_Snisar
  */
object UzToken {
  val EncodedDataPattern = Pattern.compile("\\$\\$_=.*~\\[\\];.*\"\"\\)\\(\\)\\)\\(\\);")
  val TokenPattern = Pattern.compile("[0-9a-f]{32}")


  /**
    * Supplier for token. Memorize retrieved and extracted token.
    * @return supplied
    */
  def singleton(implicit ec: ExecutionContext, mt: Materializer, as: ActorSystem): Supplier[String] = Suppliers.memoize(
    new Supplier[String] with UzToken {
      import scala.concurrent.duration._

      override def get(): String = {
        Await.result(loadToken, 10.seconds)
      }
  })


}

object UzApi {
  val RootPage = "http://booking.uz.gov.ua"

  val FindStations = "/purchase/station/{stationNameFirstLetters}/"
  val FindTrains = "/purchase/search/"
  val GetCoaches = "/purchase/coaches/"
  val GetFreeSeats = "/purchase/coach/"
}
