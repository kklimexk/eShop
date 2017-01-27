package db

import domain.models.{Product, ProductQuantity}

import scala.collection.concurrent.TrieMap

/**
  * Fake database data only for testing purpose
  */
sealed trait FakeDatabaseData {
  protected val products = TrieMap(
    Product(1, "iPhone") -> ProductQuantity(5),
    Product(2, "The Witcher") -> ProductQuantity(3),
    Product(3, "Computer") -> ProductQuantity(1),
    Product(4, "Keyboard") -> ProductQuantity(2)
  )
}

sealed trait DatabaseService {
  def checkProductAvailability(product: Product): Boolean
}

object DatabaseServiceImpl extends DatabaseService with FakeDatabaseData {
  def checkProductAvailability(product: Product): Boolean = {
    products.get(product) match {
      case Some(ProductQuantity(quantity)) if quantity > 0 =>
        products.update(product, ProductQuantity(quantity - 1))
        true
      case _ => false
    }
  }
}
