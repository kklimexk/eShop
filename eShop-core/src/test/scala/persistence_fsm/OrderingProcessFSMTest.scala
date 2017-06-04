package persistence_fsm

import actors.{DisplayOrderActor, ProductQuantityActor}

import akka.actor.{ActorRef, ActorSystem}
import akka.persistence.fsm.PersistentFSM
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}

import db.populators.Seeder

import domain.models.{DeliveryMethod, PaymentMethod}
import domain.{ChooseDeliveryMethodCommand, _}
import domain.models.response.FSMProcessInfoResponse

import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}

import shared.models.Product

class OrderingProcessFSMTest extends FunSuiteLike with TestKitBase with ImplicitSender with BeforeAndAfterAll {

  override implicit lazy val system: ActorSystem = ActorSystem(getClass.getSimpleName)

  private val product1: Product = Product(1, "iPhone", 1)
  private val product2: Product = Product(3, "Computer", 1)

  private implicit val orderId = 1123523L

  test("Ordering process FSM") {
    val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
    val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")

    val fsm = system.actorOf(OrderingProcessFSM.props(displayOrderActor, productQuantityActor), "order")

    fsm ! CreateOrderCommand(orderId)
    expectMsg(FSMProcessInfoResponse(Idle.toString, EmptyShoppingCart.toString, "order created!"))

    processStepsOfFSMOrderingProcess(fsm)

    fsm ! PersistentFSM.StateTimeout

    processStepsOfFSMOrderingProcess(fsm)

    fsm ! CheckoutCommand(orderId)
    expectMsg(FSMProcessInfoResponse(OrderReadyToCheckout.toString, DataWithPaymentMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier, PaymentMethod.CreditCard).toString, "order processed!"))
  }

  private def processStepsOfFSMOrderingProcess(fsm: ActorRef): Unit = {
    val responseMessage = "adding item to shopping cart!"

    fsm ! AddItemToShoppingCartCommand(product1)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, EmptyShoppingCart.toString, responseMessage))

    //To ensure order of added items
    Thread.sleep(300)

    fsm ! AddItemToShoppingCartCommand(product2)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1)).toString, responseMessage))

    //To ensure order of added items
    Thread.sleep(300)

    fsm ! AddItemToShoppingCartCommand(product2)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, responseMessage))

    fsm ! ConfirmShoppingCartCommand(orderId)
    expectMsg(FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "confirm shopping cart!"))

    fsm ! ChooseDeliveryMethodCommand(DeliveryMethod.Courier)
    expectMsg(FSMProcessInfoResponse(WaitingForChoosingDeliveryMethod.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "delivery method chosen!"))

    fsm ! ChoosePaymentMethodCommand(PaymentMethod.CreditCard)
    expectMsg(FSMProcessInfoResponse(WaitingForChoosingPaymentMethod.toString, DataWithDeliveryMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier).toString, "payment method chosen!"))
  }

  override protected def beforeAll(): Unit = {
    Seeder.run()
  }

  override def afterAll() {
    TestKit.shutdownActorSystem(system)
  }

}
