package peformance_tests.simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import peformance_tests.scenarios.OrderingProcessScenario

class EShopSimulation extends Simulation {

  val httpConf = http
    .baseUrl("http://0.0.0.0:8080")

  val orderingProcessScenarios = (1 to 100).map(orderId =>
    OrderingProcessScenario(orderId = orderId).inject(atOnceUsers(1))
  )

  setUp(
    orderingProcessScenarios: _*
  ).protocols(httpConf)

}
