package routers

import akka.http.scaladsl.server.{Directives, Route}
import utils.JsonSupport

trait Router extends Directives {
  def route: Route
}

trait JsonRouter extends Router with JsonSupport
