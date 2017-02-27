package domain

import models.DeliveryMethod.DeliveryMethod
import models.PaymentMethod.PaymentMethod

import shared.models.Product

sealed trait Command {
  def orderId: Long
}

case class CreateOrderCommand(orderId: Long) extends Command

case class AddItemToShoppingCartCommand(product: Product)(implicit val orderId: Long) extends Command

case class CheckoutCommand(orderId: Long) extends Command

case class ChooseDeliveryMethodCommand(deliveryMethod: DeliveryMethod)(implicit val orderId: Long) extends Command

case class ChoosePaymentMethodCommand(paymentMethod: PaymentMethod)(implicit val orderId: Long) extends Command

case class ProcessOrderCommand(orderId: Long) extends Command
