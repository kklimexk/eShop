package domain

import domain.models.DeliveryMethod.DeliveryMethod
import domain.models.PaymentMethod.PaymentMethod

sealed trait OrderingProcessFSMEvent

case object OrderCreatedEvent extends OrderingProcessFSMEvent

case class ItemAddedToShoppingCartEvent(product: models.Product) extends OrderingProcessFSMEvent

case object CheckedOutEvent extends OrderingProcessFSMEvent

case class DeliveryMethodChosenEvent(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod) extends OrderingProcessFSMEvent

case class PaymentMethodChosenEvent(data: DataWithDeliveryMethod, paymentMethod: PaymentMethod) extends OrderingProcessFSMEvent

case object OrderProcessedEvent extends OrderingProcessFSMEvent

case object OrderReadyTimeoutOccurredEvent extends OrderingProcessFSMEvent
