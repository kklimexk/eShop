package actors

import actors.ProductQuantityActor.{CheckProductAvailabilityCommand, IncreaseQuantityOfProductCommand, ProductAvailabilityCheckedEvent}
import akka.actor.{Actor, Props}
import db.DatabaseServiceImpl
import shared.models.Product

class ProductQuantityActor extends Actor {
  override def receive = {
    case CheckProductAvailabilityCommand(product) =>
      sender ! ProductAvailabilityCheckedEvent(DatabaseServiceImpl.checkProductAvailability(product))
    case IncreaseQuantityOfProductCommand(product) =>
      DatabaseServiceImpl.increaseQuantityOfProduct(product)
  }
}

object ProductQuantityActor {
  def props: Props = Props[ProductQuantityActor]

  sealed trait ProductQuantityCommand
  case class CheckProductAvailabilityCommand(product: Product) extends ProductQuantityCommand
  case class IncreaseQuantityOfProductCommand(product: Product) extends ProductQuantityCommand

  sealed trait ProductQuantityEvent
  case class ProductAvailabilityCheckedEvent(isAvailable: Boolean) extends ProductQuantityEvent
}
