package domain

import models.DeliveryMethod.DeliveryMethod
import models.PaymentMethod.PaymentMethod

import shared.models.Product

sealed trait OrderCommand {
  def orderId: Long
}

sealed trait Command

case class CreateOrderCommand(orderId: Long) extends OrderCommand

case class AddItemToShoppingCartCommand(product: Product)(implicit val orderId: Long) extends OrderCommand

case object ProductNotAvailableCommand extends Command

case class ConfirmShoppingCartCommand(orderId: Long) extends OrderCommand

case class ChooseDeliveryMethodCommand(deliveryMethod: DeliveryMethod)(implicit val orderId: Long) extends OrderCommand

case class ChoosePaymentMethodCommand(paymentMethod: PaymentMethod)(implicit val orderId: Long) extends OrderCommand

case class CheckoutCommand(orderId: Long) extends OrderCommand
