package fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}
import akka.actor.{ActorRef, FSM, Props}
import domain._
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._

@deprecated("No longer supported! There is new persistence version of this FSM actor")
class OrderingProcessFSM(displayOrderActor: ActorRef) extends FSM[OrderingProcessFSMState, OrderingProcessFSMData] {

  val orderReadyExpirationTimeout: FiniteDuration = 10.seconds

  startWith(Idle, EmptyShoppingCart)

  when(Idle) {
    case Event(CreateOrderCommand, EmptyShoppingCart) =>
      println("Creating shopping cart...")
      goto(InShoppingCart) using NonEmptyShoppingCart(products = Seq.empty) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order created or already created!")
  }

  when(InShoppingCart) {
    case Event(AddItemToShoppingCartCommand(product), s@(_: NonEmptyShoppingCart | EmptyShoppingCart)) =>
      println("Adding: " + product + " to shopping cart: " + s)
      stay using s.addItem(product) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "added item to shopping cart!")
    case Event(CheckoutCommand, s: NonEmptyShoppingCart) =>
      println("Checkout with products: " + s)
      goto(WaitingForChoosingDeliveryMethod) using s replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "checkout!")
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), s: NonEmptyShoppingCart) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) using DataWithDeliveryMethod(s, deliveryMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "delivery method chosen!")
  }

  when(WaitingForChoosingPaymentMethod) {
    case Event(ChoosePaymentMethodCommand(paymentMethod), data: DataWithDeliveryMethod) =>
      println("Payment method: " + paymentMethod)
      goto(OrderReadyToProcess) using DataWithPaymentMethod(data.shoppingCart, data.deliveryMethod, paymentMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "payment method chosen!")
  }

  when(OrderReadyToProcess, orderReadyExpirationTimeout) {
    case Event(ProcessOrderCommand, _) =>
      println("Processing order...")
      goto(OrderProcessed) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order processed!")
    case Event(StateTimeout, data: DataWithPaymentMethod) =>
      println("Timeout! Back to state InShoppingCart")
      goto(InShoppingCart) using data.empty
  }

  when(OrderProcessed) {
    case Event(OrderDisplayedEvent, _) =>
      println("Order processed!")
      stop()
  }

  onTransition {
    case OrderReadyToProcess -> OrderProcessed =>
      stateData match {
        case dataOrder@DataWithPaymentMethod(_, _, _) =>
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
      println("Order has been already created!")
      stay replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "Order has been already created!")
    case Event(e, _) =>
      println("Event: " + e + " cannot be handled in state: " + stateName)
      stay replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "Event: " + e + " cannot be handled in state: " + stateName)
  }

  initialize()
}

@deprecated("No longer supported! There is new persistence version of this FSM actor")
object OrderingProcessFSM {
  def props(displayOrderActor: ActorRef): Props = Props(classOf[OrderingProcessFSM], displayOrderActor)
}
