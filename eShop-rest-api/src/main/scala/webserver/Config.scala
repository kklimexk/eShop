package webserver

import actors.DisplayOrderActor
import routers.{MainRouter, OrderingProcessFSMRouter}
import fsm.OrderingProcessFSM
import shared.Global.Implicits.system

trait Config {
  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val orderingProcessFSM = system.actorOf(OrderingProcessFSM.props(displayOrderActor), "Order")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(orderingProcessFSM)

  //main router
  val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
