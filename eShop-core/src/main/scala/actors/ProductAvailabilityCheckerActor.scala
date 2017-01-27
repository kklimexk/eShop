package actors

import actors.ProductAvailabilityCheckerActor.{CheckProductAvailabilityCommand, ProductAvailabilityCheckedEvent}
import akka.actor.{Actor, Props}
import db.DatabaseServiceImpl
import domain.models.Product

class ProductAvailabilityCheckerActor extends Actor {
  override def receive = {
    case CheckProductAvailabilityCommand(product) =>
      sender ! ProductAvailabilityCheckedEvent(DatabaseServiceImpl.checkProductAvailability(product))
  }
}

object ProductAvailabilityCheckerActor {
  def props: Props = Props[ProductAvailabilityCheckerActor]

  sealed trait ProductQuantityCheckerCommand
  case class CheckProductAvailabilityCommand(product: Product) extends ProductQuantityCheckerCommand

  sealed trait ProductQuantityCheckerEvent
  case class ProductAvailabilityCheckedEvent(isAvailable: Boolean) extends ProductQuantityCheckerEvent
}
