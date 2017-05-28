import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

package object shared {
  trait AkkaSettings {
    implicit val system: ActorSystem
    implicit val materializer: ActorMaterializer
    implicit val executionContext: ExecutionContext
  }
  object AkkaSimpleSettings extends AkkaSettings {
    lazy val config = ConfigFactory.load()

    implicit lazy val system = ActorSystem("my-system", config)
    implicit lazy val materializer = ActorMaterializer()
    implicit lazy val executionContext = system.dispatcher
  }
  object AkkaShardedSettings extends AkkaSettings {
    lazy val config = ConfigFactory.load("sharded")

    implicit lazy val system = ActorSystem("my-system", config)
    implicit lazy val materializer = ActorMaterializer()
    implicit lazy val executionContext = system.dispatcher
  }
  object DefaultThreadPool {
    import scala.concurrent.ExecutionContext.Implicits.global

    implicit lazy val executionContext = global
  }
}
