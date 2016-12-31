package domain

import models.DeliveryMethod.DeliveryMethod
import models.PaymentMethod.PaymentMethod
import models.Product

sealed trait OrderingProcessFSMData

case object Empty extends OrderingProcessFSMData

final case class Basket(products: Seq[Product]) extends OrderingProcessFSMData {
  def addItemToBasket(product: Product): OrderingProcessFSMData = Basket(products :+ product)
}

final case class DataWithDeliveryMethod(basket: Basket, deliveryMethod: DeliveryMethod) extends OrderingProcessFSMData

final case class DataOrder(basket: Basket, deliveryMethod: DeliveryMethod, paymentMethod: PaymentMethod) extends OrderingProcessFSMData {
  def clearDataAfterTimeout = Basket(products = Seq.empty)
}
