package webserver.config

import actors.{DisplayOrderActor, ProductQuantityActor}

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.typesafe.config.ConfigFactory
import routers.{MainRouter, OrderingProcessFSMRouter}

trait SimpleConfig extends Config {

  lazy val config = ConfigFactory.load()

  implicit val system = ActorSystem("my-system", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)()

  //main router
  override val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
