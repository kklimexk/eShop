package fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}
import akka.actor.{ActorRef, FSM, Props}
import domain._
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._

class OrderingProcessFSM(displayOrderActor: ActorRef) extends FSM[OrderingProcessFSMState, OrderingProcessFSMData] {

  val orderReadyExpirationTimeout: FiniteDuration = 10.seconds

  startWith(Idle, Empty)

  when(Idle) {
    case Event(CreateOrderCommand, Empty) =>
      println("Creating Basket...")
      goto(InBasket) using Basket(products = Seq.empty) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order created or already created!")
  }

  when(InBasket) {
    case Event(AddItemToBasketCommand(product), b: Basket) =>
      println("Adding: " + product + " to basket: " + b)
      stay using b.addItemToBasket(product) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "added item to basket!")
    case Event(CheckoutCommand, b: Basket) =>
      println("Checkout with products: " + b)
      goto(WaitingForChoosingDeliveryMethod) using b replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "checkout!")
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), b: Basket) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) using DataWithDeliveryMethod(b, deliveryMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "delivery method chosen!")
  }

  when(WaitingForChoosingPaymentMethod) {
    case Event(ChoosePaymentMethodCommand(paymentMethod), data: DataWithDeliveryMethod) =>
      println("Payment method: " + paymentMethod)
      goto(OrderReadyToProcess) using DataOrder(data.basket, data.deliveryMethod, paymentMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "payment method chosen!")
  }

  when(OrderReadyToProcess, orderReadyExpirationTimeout) {
    case Event(ProcessOrderCommand, _) =>
      println("Processing order...")
      goto(OrderProcessed) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order processed!")
    case Event(StateTimeout, data: DataOrder) =>
      println("Timeout! Back to state InBasket")
      goto(InBasket) using data.clearDataAfterTimeout
  }

  when(OrderProcessed) {
    case Event(OrderDisplayedEvent, _) =>
      println("Order processed!")
      stop()
  }

  onTransition {
    case OrderReadyToProcess -> OrderProcessed =>
      stateData match {
        case dataOrder@DataOrder(_, _, _) =>
          displayOrderActor ! DisplayOrderCommand(dataOrder)
      }
  }

  onTermination {
    case StopEvent(FSM.Normal, OrderProcessed, _) =>
      println("Closing system...")
      context.system.terminate()
  }

  whenUnhandled {
    case Event(CreateOrderCommand, _) =>
      println("Basket has been already created!")
      stay replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "Basket has been already created!")
    case Event(e, _) =>
      println("Event: " + e + " cannot be handled in state: " + stateName)
      stay replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "Event: " + e + " cannot be handled in state: " + stateName)
  }

  initialize()
}

object OrderingProcessFSM {
  def props(displayOrderActor: ActorRef): Props = Props(classOf[OrderingProcessFSM], displayOrderActor)
}
