package models

object PaymentMethod extends Enumeration {
  type PaymentMethod = Value
  val CreditCard, Cash = Value
}
