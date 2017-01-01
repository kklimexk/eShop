package routers

import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

/**
  * All routers (in package routers) are combined here in MainRouter
  */
class MainRouter(routers: Router*) extends Router {
  override def route: Route = {
    routers.reduce((r1, r2) => (r1.route ~ r2.route).asInstanceOf[Router]).route
  }
}
