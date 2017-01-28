package persistence_fsm

import actors.{DisplayOrderActor, ProductQuantityActor}

import akka.actor.{ActorRef, ActorSystem}
import akka.persistence.fsm.PersistentFSM
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}

import domain.models.{DeliveryMethod, PaymentMethod, Product}
import domain.{ChooseDeliveryMethodCommand, _}
import domain.models.response.FSMProcessInfoResponse

import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

class OrderingProcessFSMTest extends FunSuiteLike with TestKitBase with ImplicitSender with BeforeAndAfterAll {

  override implicit lazy val system: ActorSystem = ActorSystem(getClass.getSimpleName)

  private val product1: Product = Product(1, "iPhone")
  private val product2: Product = Product(3, "Computer")

  test("Ordering process FSM") {
    val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
    val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

    val fsm = system.actorOf(OrderingProcessFSM.props(displayOrderActor, productQuantityActor), "order")

    fsm ! CreateOrderCommand
    expectMsg(FSMProcessInfoResponse(Idle.toString, EmptyShoppingCart.toString, "order created!"))

    processStepsOfFSMOrderingProcess(fsm)

    fsm ! PersistentFSM.StateTimeout

    processStepsOfFSMOrderingProcess(fsm)

    fsm ! ProcessOrderCommand
    expectMsg(FSMProcessInfoResponse(OrderReadyToProcess.toString, DataWithPaymentMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier, PaymentMethod.CreditCard).toString, "order processed!"))
  }

  private def processStepsOfFSMOrderingProcess(fsm: ActorRef): Unit = {
    fsm ! AddItemToShoppingCartCommand(product1)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, EmptyShoppingCart.toString, "added item to shopping cart!"))

    fsm ! AddItemToShoppingCartCommand(product2)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1)).toString, "added item to shopping cart!"))

    fsm ! AddItemToShoppingCartCommand(product2)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "product is not available!"))

    fsm ! CheckoutCommand
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "checkout!"))

    fsm ! ChooseDeliveryMethodCommand(DeliveryMethod.Courier)
    expectMsg(FSMProcessInfoResponse(WaitingForChoosingDeliveryMethod.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "delivery method chosen!"))

    fsm ! ChoosePaymentMethodCommand(PaymentMethod.CreditCard)
    expectMsg(FSMProcessInfoResponse(WaitingForChoosingPaymentMethod.toString, DataWithDeliveryMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier).toString, "payment method chosen!"))
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}
