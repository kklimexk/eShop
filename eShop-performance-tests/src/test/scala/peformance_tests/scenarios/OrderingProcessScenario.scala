package peformance_tests.scenarios

import io.gatling.core.Predef._
import io.gatling.core.session.StaticStringExpression
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.Predef._

import scala.util.Random

class OrderingProcessScenario extends OrderScenario {

  private val product1 = """{ "id": 1, "name": "iPhone" }"""
  private val product2 = """{ "id": 2, "name": "The Witcher" }"""
  private val product3 = """{ "id": 3, "name": "Computer" }"""
  private val product4 = """{ "id": 4, "name": "Keyboard" }"""

  private val productsJson = List(product1, product2, product3, product4)

  private val deliveryMethod = """{ "name": "Courier" }"""
  private val paymentMethod = """{ "name": "CreditCard" }"""

  override def orderScenarioBuilder(orderId: Int): ScenarioBuilder = {

    val orderPrefix = "/orderId/" + orderId

    scenario("OrderingProcess" + orderId)
      .exec(http("createOrder").post(StaticStringExpression("/createOrder/" + orderId)))
      .pause(3)
      .exec(addItemToShoppingCart(orderId))
      .pause(3)
      .exec(http("confirmShoppingCart")
        .post(StaticStringExpression(orderPrefix + "/confirmShoppingCart")))
      .pause(3)
      .exec(http("deliveryMethod")
        .post(StaticStringExpression(orderPrefix + "/deliveryMethod"))
        .body(StringBody(deliveryMethod)).asJSON)
      .pause(3)
      .exec(http("paymentMethod")
        .post(StaticStringExpression(orderPrefix + "/paymentMethod"))
        .body(StringBody(paymentMethod)).asJSON)
      .pause(3)
      .exec(http("checkout")
        .post(StaticStringExpression(orderPrefix + "/checkout")))
      .pause(5)
  }

  private def addItemToShoppingCart(orderId: Int) = repeat(times = 10) {
    exec(http("addItemToShoppingCart")
      .post(StaticStringExpression("/orderId/" + orderId + "/addItemToShoppingCart"))
      .body(StringBody(productsJson.apply(Random.nextInt(productsJson.size)))).asJSON)
      .pause(3)
  }
}

object OrderingProcessScenario {
  def apply: OrderingProcessScenario = new OrderingProcessScenario()
  def apply(orderId: Int): ScenarioBuilder = new OrderingProcessScenario().orderScenarioBuilder(orderId)
}
