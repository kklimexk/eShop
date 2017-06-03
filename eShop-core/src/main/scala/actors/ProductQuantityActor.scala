package actors

import actors.ProductQuantityActor.{CheckProductAvailabilityCommand, IncreaseQuantityOfProductCommand, ProductAvailabilityCheckedEvent}
import akka.actor.{Actor, Props}
import db.services.ProductDatabaseService

import scala.concurrent.Future

import shared.models.Product
import shared.DefaultThreadPool._

class ProductQuantityActor extends Actor {
  override def receive = {
    case CheckProductAvailabilityCommand(product) =>
      sender ! ProductAvailabilityCheckedEvent(ProductDatabaseService.decreaseIfAvailable(product.id))
    case IncreaseQuantityOfProductCommand(product) =>
      ProductDatabaseService.changeQuantityOfProduct(product.id).onComplete(_ => ())
  }
}

object ProductQuantityActor {
  def props: Props = Props[ProductQuantityActor]

  sealed trait ProductQuantityCommand
  case class CheckProductAvailabilityCommand(product: Product) extends ProductQuantityCommand
  case class IncreaseQuantityOfProductCommand(product: Product) extends ProductQuantityCommand

  sealed trait ProductQuantityEvent
  case class ProductAvailabilityCheckedEvent(isAvailable: Future[Boolean]) extends ProductQuantityEvent
}
