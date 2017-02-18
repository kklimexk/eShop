package webserver.config.sharded

import akka.cluster.sharding.ClusterSharding
import akka.util.Timeout

import persistence_fsm.OrderingProcessFSM
import routers.{MainRouter, OrderingProcessFSMRouter}
import webserver.config.Config

import shared.AkkaShardedSettings._

import scala.concurrent.Await
import scala.concurrent.duration._

trait WebServerShardedConfig extends Config {

  implicit val resolveTimeout = Timeout(5.seconds)

  val displayOrderActor = Await.result(system.actorSelection("/user/DisplayOrderActor").resolveOne(), resolveTimeout.duration)
  val productQuantityActor = Await.result(system.actorSelection("/user/ProductQuantityActor").resolveOne(), resolveTimeout.duration)

  val orderingProcessRegion = Some(ClusterSharding(system).shardRegion(OrderingProcessFSM.shardName))

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)(orderingProcessRegion)

  //main router
  override val mainRouter = new MainRouter(orderingProcessFSMRouter)
}
