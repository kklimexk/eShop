package domain

import models.DeliveryMethod.DeliveryMethod
import models.PaymentMethod.PaymentMethod
import models.Product

sealed trait OrderingProcessFSMData {
  def addItem(product: Product): OrderingProcessFSMData
  def empty(): OrderingProcessFSMData
  final def withDeliveryMethod(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod) = {
    DataWithDeliveryMethod(shoppingCart, deliveryMethod)
  }
  final def withPaymentMethod(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod, paymentMethod: PaymentMethod) = {
    DataWithPaymentMethod(shoppingCart, deliveryMethod, paymentMethod)
  }
}

case object EmptyShoppingCart extends OrderingProcessFSMData {
  def addItem(product: Product) = NonEmptyShoppingCart(product :: Nil)
  def empty() = this
}

final case class NonEmptyShoppingCart(products: Seq[Product]) extends OrderingProcessFSMData {
  def addItem(product: Product) = NonEmptyShoppingCart(products :+ product)
  def empty() = EmptyShoppingCart
}

final case class DataWithDeliveryMethod(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod) extends OrderingProcessFSMData {
  def addItem(product: Product) = shoppingCart.addItem(product)
  def empty() = shoppingCart.empty()
}

final case class DataWithPaymentMethod(shoppingCart: NonEmptyShoppingCart, deliveryMethod: DeliveryMethod, paymentMethod: PaymentMethod) extends OrderingProcessFSMData {
  def addItem(product: Product) = shoppingCart.addItem(product)
  def empty() = shoppingCart.empty()
}
