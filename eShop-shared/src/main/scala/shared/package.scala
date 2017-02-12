import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext

package object shared {
  trait AkkaSettings {
    implicit val system: ActorSystem
    implicit val materializer: ActorMaterializer
    implicit val executionContext: ExecutionContext
  }
}
