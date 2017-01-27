package webserver

import actors.{DisplayOrderActor, ProductAvailabilityCheckerActor}
import routers.{MainRouter, OrderingProcessFSMRouter}
import shared.Global.Implicits.system

trait Config {
  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val productAvailabilityCheckerActor = system.actorOf(ProductAvailabilityCheckerActor.props, "ProductAvailabilityCheckerActor")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productAvailabilityCheckerActor)

  //main router
  val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
