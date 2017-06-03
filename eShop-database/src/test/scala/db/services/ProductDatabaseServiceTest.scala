package db.services

import db.populators.Seeder

import org.scalatest.compatible.Assertion
import org.scalatest.{AsyncFunSuiteLike, BeforeAndAfterAll}

import scala.concurrent.Future

class ProductDatabaseServiceTest extends AsyncFunSuiteLike with BeforeAndAfterAll {

  private val productDbService = ProductDatabaseService

  override protected def beforeAll(): Unit = Seeder.run()

  test("find product by id") {
    findProductById(1, "iphone")
    findProductById(2, "the witcher")
    findProductById(3, "computer")
    findProductById(4, "keyboard")
  }

  private def findProductById(id: Long, name: String): Future[Assertion] = {
    productDbService.findById(id).map { productOpt =>
      val product = productOpt.getOrElse(throw new RuntimeException("Product does not exist!"))
      assert(product.id == id)
      assert(product.name.toLowerCase == name.toLowerCase)
    }
  }

  test("check product availability") {
    checkProductAvailability(id = 3, expectedResult = true)
    checkProductAvailability(id = 5, expectedResult = false)
  }

  private def checkProductAvailability(id: Long, expectedResult: Boolean): Future[Assertion] = {
    productDbService.decreaseIfAvailable(id).map { result =>
      assert(result == expectedResult)
    }
  }

  test("increase/decrease quantity of product") {
    val productId = 1
    val productId2 = 2

    for {
      _ <- changeQuantityOfProduct(productId, by = 2, expectedQuantity = 7)
      _ <- changeQuantityOfProduct(productId2, by = 97, expectedQuantity = 100)
      res <- changeQuantityOfProduct(productId, by = -7, expectedQuantity = 0)
    } yield res
  }

  private def changeQuantityOfProduct(id: Long, by: Int, expectedQuantity: Int): Future[Assertion] = {
    productDbService.changeQuantityOfProduct(id, by).flatMap { _ =>
      productDbService.findById(id).map {
        case Some(product) => assert(product.quantity == expectedQuantity)
        case _ => throw new RuntimeException("Product does not exist!")
      }
    }
  }

}
