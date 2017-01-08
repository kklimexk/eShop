package fsm

import actors.DisplayOrderActor
import akka.actor.ActorSystem
import akka.actor.FSM.StateTimeout
import akka.testkit.{TestActorRef, TestFSMRef, TestKit}
import domain.models.{DeliveryMethod, PaymentMethod}
import domain._
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class OrderingProcessFSMTest extends TestKit(ActorSystem("TestActorSystem")) with FunSuiteLike with BeforeAndAfterAll {

  test("Creating order with two items") {
    val displayOrderActorTest = TestActorRef[DisplayOrderActor](DisplayOrderActor.props)
    val fsm = TestFSMRef(new OrderingProcessFSM(displayOrderActorTest.underlyingActor.self))

    val orderingProcessFSMTest: TestActorRef[OrderingProcessFSM] = fsm

    assert(fsm.stateName == Idle)

    fsm ! CreateOrderCommand

    assert(fsm.stateName == InShoppingCart)

    fsm ! AddItemToShoppingCartCommand(models.Product(1, "iPhone 5s"))
    fsm ! AddItemToShoppingCartCommand(models.Product(2, "The Witcher 3"))

    assert(fsm.stateName == InShoppingCart)

    fsm ! CheckoutCommand

    assert(fsm.stateName == WaitingForChoosingDeliveryMethod)

    fsm ! ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.Courier)

    assert(fsm.stateName == WaitingForChoosingPaymentMethod)

    fsm ! ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.CreditCard)

    assert(fsm.stateName == OrderReadyToProcess)

    fsm ! ProcessOrderCommand
  }

  test("Timeout in OrderReadyToProcess state and back to InBasket state") {
    val displayOrderActorTest = TestActorRef[DisplayOrderActor](DisplayOrderActor.props)
    val fsm = TestFSMRef(new OrderingProcessFSM(displayOrderActorTest.underlyingActor.self))

    val orderingProcessFSMTest: TestActorRef[OrderingProcessFSM] = fsm

    assert(fsm.stateName == Idle)

    fsm ! CreateOrderCommand

    assert(fsm.stateName == InShoppingCart)

    fsm ! AddItemToShoppingCartCommand(models.Product(1, "iPhone 5s"))
    fsm ! AddItemToShoppingCartCommand(models.Product(2, "The Witcher 3"))

    assert(fsm.stateName == InShoppingCart)

    fsm ! CheckoutCommand

    assert(fsm.stateName == WaitingForChoosingDeliveryMethod)

    fsm ! ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.Courier)

    assert(fsm.stateName == WaitingForChoosingPaymentMethod)

    fsm ! ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.CreditCard)

    assert(fsm.stateName == OrderReadyToProcess)

    fsm ! StateTimeout

    assert(fsm.stateName == InShoppingCart)
  }

}
