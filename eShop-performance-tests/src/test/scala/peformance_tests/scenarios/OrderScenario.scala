package peformance_tests.scenarios

import io.gatling.core.structure.ScenarioBuilder

trait OrderScenario {
  def orderScenarioBuilder(orderId: Int): ScenarioBuilder
}
