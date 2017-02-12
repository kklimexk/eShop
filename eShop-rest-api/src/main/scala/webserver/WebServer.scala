package webserver

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route

import webserver.config.Config

import scala.io.StdIn

class WebServer { config: Config =>
  def run = {
    val route: Route = mainRouter.route
    val bindingFuture = Http().bindAndHandle(route, "0.0.0.0", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
