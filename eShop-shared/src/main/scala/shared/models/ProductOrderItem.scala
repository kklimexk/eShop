package shared.models

/**
  * This entity represents single item of concrete product
  *
  * @param id unique identifier for the concrete product
  * @param name
  */
case class ProductOrderItem(id: Long, name: String) extends Entity {
  def toProduct: Product = Product.apply(id, name, quantity = 1)
}
