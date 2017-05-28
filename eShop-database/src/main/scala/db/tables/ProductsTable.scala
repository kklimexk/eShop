package db.tables

import shared.models.Product

import slick.lifted.Tag
import db.currentJdbcProfile.api._

private[db] class ProductsTable(tag: Tag) extends Table[Product](tag, "products") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")
  def quantity = column[Int]("quantity")

  def * = (id, name, quantity) <> (Product.tupled, Product.unapply)
}
