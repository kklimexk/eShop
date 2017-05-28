package shared.models

/**
  * This entity represents concrete product with quantity in database
  *
  * @param id unique identifier for the concrete product
  * @param name
  * @param quantity available units of concrete product (not used in FakeDatabase.scala)
  */
case class Product(id: Long, name: String, quantity: Int = 0) extends Entity
