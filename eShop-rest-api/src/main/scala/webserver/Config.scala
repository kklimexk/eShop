package webserver

import actors.{DisplayOrderActor, ProductQuantityActor}
import routers.{MainRouter, OrderingProcessFSMRouter}
import shared.Global.Implicits.system

trait Config {
  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)

  //main router
  val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
