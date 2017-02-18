package webserver.config.simple

import actors.{DisplayOrderActor, ProductQuantityActor}
import routers.{MainRouter, OrderingProcessFSMRouter}
import webserver.config.Config

import shared.AkkaSimpleSettings._

trait WebServerSimpleConfig extends Config {

  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)()

  //main router
  override val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
