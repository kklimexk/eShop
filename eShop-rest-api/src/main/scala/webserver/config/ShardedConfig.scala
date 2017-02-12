package webserver.config

import actors.{DisplayOrderActor, ProductQuantityActor}

import akka.actor.{ActorIdentity, ActorPath, ActorSystem, Identify, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings}
import akka.pattern.ask
import akka.persistence.journal.leveldb.{SharedLeveldbJournal, SharedLeveldbStore}
import akka.stream.ActorMaterializer
import akka.util.Timeout

import com.typesafe.config.ConfigFactory
import persistence_fsm.OrderingProcessFSM
import routers.{MainRouter, OrderingProcessFSMRouter}

import scala.concurrent.duration._

trait ShardedConfig extends Config {

  lazy val config = ConfigFactory.load("sharded")

  implicit val system = ActorSystem("my-system", config)
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

  startupSharedJournal(startStore = true, ActorPath.fromString("akka.tcp://my-system@127.0.0.1:2551/user/store"))

  ClusterSharding(system).start(
    typeName = OrderingProcessFSM.shardName,
    entityProps = OrderingProcessFSM.props(displayOrderActor, productQuantityActor),
    settings = ClusterShardingSettings(system),
    extractShardId = OrderingProcessFSM.extractShardId,
    extractEntityId = OrderingProcessFSM.extractEntityId
  )

  val orderingProcessRegion = Some(ClusterSharding(system).shardRegion(OrderingProcessFSM.shardName))

  //routers
  val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)(orderingProcessRegion)

  //main router
  override val mainRouter = new MainRouter(orderingProcessFSMRouter)

  private def startupSharedJournal(startStore: Boolean, path: ActorPath): Unit = {
    // Start the shared journal one node (don't crash this SPOF)
    // This will not be needed with a distributed journal
    if (startStore) system.actorOf(Props[SharedLeveldbStore], "store")
    // register the shared journal
    implicit val timeout = Timeout(15.seconds)
    val f = system.actorSelection(path) ? Identify(None)
    f.onSuccess {
      case ActorIdentity(_, Some(ref)) => SharedLeveldbJournal.setStore(ref, system)
      case _ =>
        system.log.error("Shared journal not started at {}", path)
        system.terminate()
    }
    f.onFailure {
      case _ =>
        system.log.error("Lookup of shared journal at {} timed out", path)
        system.terminate()
    }
  }
}
