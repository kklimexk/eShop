package shared

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Global {
  object Implicits {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
  }
}
