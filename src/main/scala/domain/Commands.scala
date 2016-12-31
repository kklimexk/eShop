package domain

import models.DeliveryMethod.DeliveryMethod
import models.PaymentMethod.PaymentMethod

sealed trait Command

case object CreateOrderCommand extends Command

case class AddItemToBasketCommand(product: models.Product) extends Command

case object CheckoutCommand extends Command

case class ChooseDeliveryMethodCommand(deliveryMethod: DeliveryMethod) extends Command

case class ChoosePaymentMethodCommand(paymentMethod: PaymentMethod) extends Command

case object ProcessOrderCommand extends Command
