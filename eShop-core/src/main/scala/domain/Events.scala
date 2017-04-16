package domain

import domain.models.DeliveryMethod.DeliveryMethod
import domain.models.PaymentMethod.PaymentMethod

import shared.models.Product

sealed trait OrderingProcessFSMEvent

case object OrderCreatedEvent extends OrderingProcessFSMEvent

case class ItemAddedToShoppingCartEvent(product: Product) extends OrderingProcessFSMEvent

case object ProductNotAvailableEvent extends OrderingProcessFSMEvent

case object ConfirmedShoppingCartEvent extends OrderingProcessFSMEvent

case class DeliveryMethodChosenEvent(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod) extends OrderingProcessFSMEvent

case class PaymentMethodChosenEvent(data: DataWithDeliveryMethod, paymentMethod: PaymentMethod) extends OrderingProcessFSMEvent

case object CheckedOutEvent extends OrderingProcessFSMEvent

case object OrderReadyTimeoutOccurredEvent extends OrderingProcessFSMEvent
