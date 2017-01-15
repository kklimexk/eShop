package webserver

import actors.DisplayOrderActor
import routers.{MainRouter, OrderingProcessFSMRouter}
import shared.Global.Implicits.system

trait Config {
  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor)

  //main router
  val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
