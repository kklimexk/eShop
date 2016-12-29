package fsm

import akka.actor.{FSM, Props}
import domain._

import scala.concurrent.duration._

class OrderingProcessFSM extends FSM[OrderingProcessFSMState, OrderingProcessFSMData] {

  val orderReadyExpirationTimeout: FiniteDuration = 10.seconds

  startWith(Idle, Empty)

  when(Idle) {
    case Event(CreateBasketCommand, Empty) =>
      println("Creating Basket...")
      goto(InBasket) using Basket(products = Seq.empty)
  }

  when(InBasket) {
    case Event(AddItemToBasketCommand(product), b: Basket) =>
      println("Adding: " + product + " to basket: " + b)
      stay using b.addItemToBasket(product)
    case Event(CheckoutCommand, b: Basket) =>
      println("Checkout with products: " + b)
      goto(WaitingForChoosingDeliveryMethod) using b
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), b: Basket) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) using DataWithDeliveryMethod(b, deliveryMethod)
  }

  when(WaitingForChoosingPaymentMethod) {
    case Event(ChoosePaymentMethodCommand(paymentMethod), data: DataWithDeliveryMethod) =>
      println("Payment method: " + paymentMethod)
      goto(OrderReadyToProcess) using DataOrder(data.basket, data.deliveryMethod, paymentMethod)
  }

  when(OrderReadyToProcess, orderReadyExpirationTimeout) {
    case Event(ProcessOrderCommand, data: DataOrder) =>
      println("Processing order...")
      goto(OrderProcessed) using data
    case Event(StateTimeout, data: DataOrder) =>
      println("Timeout! Back to state InBasket")
      goto(InBasket) using data.clearDataAfterTimeout
  }

  when(OrderProcessed) {
    case Event(PrintOutOrderCommand, data: DataOrder) =>
      println("Order: " + data + " processed")
      stop()
  }

  onTermination {
    case StopEvent(FSM.Normal, OrderProcessed, data) =>
      println("Closing system...")
      context.system.terminate()
  }

  whenUnhandled {
    case Event(CreateBasketCommand, _) =>
      println("Basket has been already created!")
      stay
    case Event(e, _) =>
      println("Event: " + e + " cannot be handled in state: " + stateName)
      stay
  }

  initialize()
}

object OrderingProcessFSM {
  def props: Props = Props[OrderingProcessFSM]
}
