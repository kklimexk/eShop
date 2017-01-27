package db

import domain.models.{Product, ProductQuantity}

import scala.collection.concurrent.TrieMap

trait DatabaseService {
  def checkProductAvailability(product: Product): Boolean
}

object FakeDatabase extends DatabaseService {

  private val products = TrieMap(
    Product(1, "iPhone") -> ProductQuantity(5),
    Product(2, "The Witcher") -> ProductQuantity(3),
    Product(3, "Computer") -> ProductQuantity(1),
    Product(4, "Keyboard") -> ProductQuantity(2)
  )

  def checkProductAvailability(product: Product): Boolean = {
    products.get(product) match {
      case Some(ProductQuantity(quantity)) if quantity > 0 =>
        products.update(product, ProductQuantity(quantity - 1))
        true
      case _ => false
    }
  }

}
