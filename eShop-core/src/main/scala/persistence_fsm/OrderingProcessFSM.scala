package persistence_fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}

import akka.actor.{ActorRef, Props}
import akka.persistence.DeleteMessagesSuccess
import akka.persistence.fsm.PersistentFSM

import domain.{CheckedOutEvent, DeliveryMethodChosenEvent, _}
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._
import scala.reflect._

class OrderingProcessFSM(displayOrderActor: ActorRef) extends PersistentFSM[OrderingProcessFSMState, OrderingProcessFSMData, OrderingProcessFSMEvent] {

  override def domainEventClassTag: ClassTag[OrderingProcessFSMEvent] = classTag[OrderingProcessFSMEvent]
  override def persistenceId: String = "OrderingProcessFSM" + self.path

  val orderReadyExpirationTimeout: FiniteDuration = 10.seconds

  override def applyEvent(domainEvent: OrderingProcessFSMEvent, currentData: OrderingProcessFSMData): OrderingProcessFSMData = {
    domainEvent match {
      case OrderCreatedEvent => currentData.empty()
      case ItemAddedToShoppingCartEvent(product) => currentData.addItem(product)
      case CheckedOutEvent => currentData
      case DeliveryMethodChosenEvent(s, deliveryMethod) => currentData.withDeliveryMethod(s, deliveryMethod)
      case PaymentMethodChosenEvent(data, paymentMethod) => currentData.withPaymentMethod(data.shoppingCart, data.deliveryMethod, paymentMethod)
      case OrderProcessedEvent => currentData
      case OrderReadyTimeoutOccurredEvent => currentData.empty()
    }
  }

  startWith(Idle, EmptyShoppingCart)

  when(Idle) {
    case Event(CreateOrderCommand, EmptyShoppingCart) =>
      println("Creating shopping cart...")
      goto(InShoppingCart) applying OrderCreatedEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order created!")
  }

  when(InShoppingCart) {
    case Event(AddItemToShoppingCartCommand(product), s@(_: NonEmptyShoppingCart | EmptyShoppingCart)) =>
      println("Adding: " + product + " to shopping cart: " + s)
      stay applying ItemAddedToShoppingCartEvent(product) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "added item to shopping cart!")
    case Event(CheckoutCommand, s: NonEmptyShoppingCart) =>
      println("Checkout with products: " + s)
      goto(WaitingForChoosingDeliveryMethod) applying CheckedOutEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "checkout!")
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), s: NonEmptyShoppingCart) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) applying DeliveryMethodChosenEvent(s, deliveryMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "delivery method chosen!")
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
      println("Timeout! Back to state InShoppingCart")
      goto(InShoppingCart) applying OrderReadyTimeoutOccurredEvent
  }

  when(OrderProcessed) {
    case Event(OrderDisplayedEvent, _) =>
      println("Order processed!")
      deleteMessages(this.lastSequenceNr)
      stay
  }

  onTransition {
    case OrderReadyToProcess -> OrderProcessed =>
      stateData match {
        case dataOrder@DataWithPaymentMethod(_, _, _) =>
          displayOrderActor ! DisplayOrderCommand(dataOrder)
      }
  }

  onTermination {
    case StopEvent(PersistentFSM.Normal, OrderProcessed, _) =>
      println("Closing order...")
  }

  whenUnhandled {
    case Event(CreateOrderCommand, _) =>
      println("Order has been already created!")
      stay replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "Order has been already created!")
    case Event(DeleteMessagesSuccess(toSequenceNr), _) =>
      println("All messages from journal deleted!" + " toSequenceNr: " + toSequenceNr)
      stop()
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
