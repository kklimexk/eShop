package domain.models

case class PaymentMethodEntity(name: String)

object PaymentMethod extends Enumeration {
  type PaymentMethod = Value
  val CreditCard, Cash = Value
}
