package utils

import akka.actor.ActorNotFound
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask

import domain.Command

import routers.JsonRouter
import shared.Global.Implicits.system

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

trait ResponseUtil { jsonRouter: JsonRouter =>

  def response[T](commandResult: Future[T])(implicit ev$1: T => ToResponseMarshallable, timeout: Timeout): Route = {
    onComplete(commandResult) {
      case Success(response) =>
        complete(response)
      case Failure(_) =>
        complete(StatusCodes.InternalServerError)
    }
  }

  def extendedResponse[T: ClassTag, K: ClassTag](selectionActorPath: String, command: Command)(orElseResponse: => Future[K])
                                                (implicit ev$1: T => ToResponseMarshallable, ev$2: K => ToResponseMarshallable, timeout: Timeout): Future[Route] = {
    system.actorSelection(selectionActorPath).resolveOne()
      .map(actor => response((actor ? command).mapTo[T]))
      .recover {
        case _: ActorNotFound =>
          response(orElseResponse)
      }
  }

}
