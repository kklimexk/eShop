package db.services

import db.JdbcDatabaseConfigProvider
import db.tables.ProductsTable
import db.currentJdbcProfile.api._

import shared.models.Product
import shared.DefaultThreadPool._

import scala.concurrent.Future

object ProductDatabaseService extends DatabaseService[Product, ProductsTable](tag => new ProductsTable(tag)) with JdbcDatabaseConfigProvider {
  def decreaseIfAvailable(productId: Long): Future[Boolean] =
    changeQuantityOfProduct(productId, -1)
  def changeQuantityOfProduct(productId: Long, by: Int = 1): Future[Boolean] = {
    val updateQuantityQuery =
      sqlu"""UPDATE #$tableName SET quantity = quantity + $by
            WHERE id = $productId AND quantity + $by >= 0"""
    db.run(updateQuantityQuery).map(res => if (res == 1) true else false)
  }
}
