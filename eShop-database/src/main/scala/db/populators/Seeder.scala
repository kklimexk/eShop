package db.populators

import db.JdbcDatabaseConfigProvider
import db.tables.ProductsTable
import db.currentJdbcProfile.api._

import shared.models.Product

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Seeder extends JdbcDatabaseConfigProvider {

  private val products = TableQuery[ProductsTable]

  private def dropTables() = {
    sqlu"""DROP TABLE IF EXISTS #${products.baseTableRow.tableName}"""
  }

  private def createSchemas() = {
    products.schema.create
  }

  private def insertData() = {
    DBIO.seq(
      products += Product(1, "iPhone", 5),
      products += Product(2, "The Witcher", 3),
      products += Product(3, "Computer", 1),
      products += Product(4, "Keyboard", 2),
      products += Product(5, "Laptop")
    )
  }

  def run() = {
    try {

      val setup = DBIO.seq(
        dropTables(),
        createSchemas(),
        insertData()
      )

      val setupF = db.run(setup)
      Await.result(setupF, Duration.Inf)

    } finally db.close()
  }

  def main(args: Array[String]): Unit = {
    run()
  }
}
