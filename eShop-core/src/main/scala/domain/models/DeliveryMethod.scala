package domain.models


case class DeliveryMethodEntity(name: String)

object DeliveryMethod extends Enumeration {
  type DeliveryMethod = Value
  val Courier, PersonalCollection = Value
}
