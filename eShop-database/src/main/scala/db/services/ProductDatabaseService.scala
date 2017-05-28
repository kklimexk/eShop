package db.services

import db.JdbcDatabaseConfigProvider
import db.tables.ProductsTable

import shared.DefaultThreadPool._

import db.currentJdbcProfile.api._

import shared.models.Product

import scala.concurrent.Future

object ProductDatabaseService extends DatabaseService[Product, ProductsTable](tag => new ProductsTable(tag)) with JdbcDatabaseConfigProvider {
  def decreaseIfAvailable(productId: Long): Future[Boolean] = {
    findById(productId).flatMap {
      case Some(product) if product.quantity > 0 =>
        changeQuantityOfProduct(productId, -1).map(_ => true)
      case _ => Future.successful(false)
    }
  }
  def changeQuantityOfProduct(productId: Long, by: Int = 1): Future[Unit] = {
    val updateQueryF = findById(productId).map {
      case Some(product) if (product.quantity + by) >= 0 =>
        findByIdQuery(productId).update(product.copy(quantity = product.quantity + by))
      case Some(product) if (product.quantity + by) < 0 =>
        throw new RuntimeException("Quantity of product cannot be negative!")
      case _ => throw new RuntimeException("Product does not exist!")
    }
    updateQueryF.flatMap(updateQuery => db.run(updateQuery).map(_ => ()))
  }
}
