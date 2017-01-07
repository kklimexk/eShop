package persistence_fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}

import akka.actor.{ActorRef, Props}
import akka.persistence.fsm.PersistentFSM

import domain.{CheckedOutEvent, DeliveryMethodChosenEvent, _}
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._
import scala.reflect._

class OrderingProcessFSM(displayOrderActor: ActorRef) extends PersistentFSM[OrderingProcessFSMState, OrderingProcessFSMData, OrderingProcessFSMEvent] {

  override def domainEventClassTag: ClassTag[OrderingProcessFSMEvent] = classTag[OrderingProcessFSMEvent]
  override def persistenceId: String = "OrderingProcessFSM"

  val orderReadyExpirationTimeout: FiniteDuration = 10.seconds

  override def applyEvent(domainEvent: OrderingProcessFSMEvent, currentData: OrderingProcessFSMData): OrderingProcessFSMData = {
    domainEvent match {
      case OrderCreatedEvent => Basket(products = Seq.empty)
      case ItemAddedToBasketEvent(basket, product) => basket.addItemToBasket(product)
      case CheckedOutEvent => currentData
      case DeliveryMethodChosenEvent(basket, deliveryMethod) => DataWithDeliveryMethod(basket, deliveryMethod)
      case PaymentMethodChosenEvent(data, paymentMethod) => DataOrder(data.basket, data.deliveryMethod, paymentMethod)
      case OrderProcessedEvent => currentData
      case OrderReadyTimeoutOccurredEvent => Basket(products = Seq.empty)
    }
  }

  startWith(Idle, Empty)

  when(Idle) {
    case Event(CreateOrderCommand, Empty) =>
      println("Creating Basket...")
      goto(InBasket) applying OrderCreatedEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order created!")
  }

  when(InBasket) {
    case Event(AddItemToBasketCommand(product), b: Basket) =>
      println("Adding: " + product + " to basket: " + b)
      stay applying ItemAddedToBasketEvent(b, product) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "added item to basket!")
    case Event(CheckoutCommand, b: Basket) =>
      println("Checkout with products: " + b)
      goto(WaitingForChoosingDeliveryMethod) applying CheckedOutEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "checkout!")
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), b: Basket) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) applying DeliveryMethodChosenEvent(b, deliveryMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "delivery method chosen!")
  }

  when(WaitingForChoosingPaymentMethod) {
    case Event(ChoosePaymentMethodCommand(paymentMethod), data: DataWithDeliveryMethod) =>
      println("Payment method: " + paymentMethod)
      goto(OrderReadyToProcess) applying PaymentMethodChosenEvent(data, paymentMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "payment method chosen!")
  }

  when(OrderReadyToProcess, orderReadyExpirationTimeout) {
    case Event(ProcessOrderCommand, _) =>
      println("Processing order...")
      goto(OrderProcessed) applying OrderProcessedEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order processed!")
    case Event(StateTimeout, _) =>
      println("Timeout! Back to state InBasket")
      goto(InBasket) applying OrderReadyTimeoutOccurredEvent
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
    case StopEvent(PersistentFSM.Normal, OrderProcessed, _) =>
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

  override def onRecoveryCompleted(): Unit = println("Recovery completed!")

  override def onRecoveryFailure(cause: Throwable, event: Option[Any]): Unit = {
    println("Recovery failed!")
    super.onRecoveryFailure(cause, event)
  }

}

object OrderingProcessFSM {
  def props(displayOrderActor: ActorRef): Props = Props(classOf[OrderingProcessFSM], displayOrderActor)
}
