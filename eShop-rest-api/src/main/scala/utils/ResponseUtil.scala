package utils

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.util.Timeout

import domain.models.response.FSMProcessInfoResponse
import routers.JsonRouter

import scala.concurrent.Future
import scala.util.{Failure, Success}

trait ResponseUtil { jsonRouter: JsonRouter =>
  def response(commandResult: Future[Any])(implicit timeout: Timeout): Route = {
    val responseF = commandResult.mapTo[FSMProcessInfoResponse]
    onComplete(responseF) {
      case Success(response) =>
        complete(response)
      case Failure(_) =>
        complete(StatusCodes.InternalServerError)
    }
  }
}
