package actors

import actors.ProductQuantityActor.{CheckProductAvailabilityCommand, IncreaseQuantityOfProductCommand, ProductAvailabilityCheckedEvent}
import akka.actor.{Actor, Props}
import db.services.ProductDatabaseService
import shared.models.Product

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ProductQuantityActor extends Actor {
  override def receive = {
    case CheckProductAvailabilityCommand(product) =>
      sender ! ProductAvailabilityCheckedEvent(ProductDatabaseService.decreaseIfAvailable(product.id))
    case IncreaseQuantityOfProductCommand(product) =>
      Await.ready(ProductDatabaseService.changeQuantityOfProduct(product.id), Duration.Inf)
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
