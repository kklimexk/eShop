package routers

import akka.http.scaladsl.server.Route

trait Router {
  def route: Route
}
