package org.tickets.bot.uz

import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import org.tickets.misc.HttpSupport

import scala.util.{Failure, Success}

/**
  * Created by bsnisar on 29.09.16.
  */
object TokenFlow {
  val UzRootPage = "/"

  def getToken() = {
    Http().singleRequest(RequestBuilding.Get(UzRootPage)) onComplete {
      case Success(response) =>
        Unmarshal(response).to[ByteString]


      case Failure(error) => error.printStackTrace()
    }

  }

  def foo() = {




    Flow.apply[HttpSupport.Request].mapAsyncUnordered[HttpSupport.Request](100) { req =>

      ???
    }


  }
}
