package persistence_fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}
import actors.ProductQuantityActor.{CheckProductAvailabilityCommand, IncreaseQuantityOfProductCommand, ProductAvailabilityCheckedEvent}

import akka.actor.{ActorRef, Props}
import akka.cluster.sharding.ShardRegion
import akka.persistence.DeleteMessagesSuccess
import akka.persistence.fsm.PersistentFSM
import akka.pattern.ask
import akka.util.Timeout

import domain.{ConfirmedShoppingCartEvent, DeliveryMethodChosenEvent, _}
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect._

class OrderingProcessFSM(displayOrderActor: ActorRef,
                         productQuantityActor: ActorRef) extends PersistentFSM[OrderingProcessFSMState, OrderingProcessFSMData, OrderingProcessFSMEvent] {

  override def domainEventClassTag: ClassTag[OrderingProcessFSMEvent] = classTag[OrderingProcessFSMEvent]
  override def persistenceId: String = "OrderingProcessFSM" + self.path

  implicit val timeout = Timeout(10.seconds)

  override def applyEvent(domainEvent: OrderingProcessFSMEvent, currentData: OrderingProcessFSMData): OrderingProcessFSMData = {
    domainEvent match {
      case OrderCreatedEvent => currentData.empty()
      case ItemAddedToShoppingCartEvent(product) => currentData.addItem(product)
      case ProductNotAvailableEvent => currentData
      case ConfirmedShoppingCartEvent => currentData
      case DeliveryMethodChosenEvent(s, deliveryMethod) => currentData.withDeliveryMethod(s, deliveryMethod)
      case PaymentMethodChosenEvent(data, paymentMethod) => currentData.withPaymentMethod(data.shoppingCart, data.deliveryMethod, paymentMethod)
      case CheckedOutEvent => currentData
      case OrderReadyTimeoutOccurredEvent => currentData.empty()
    }
  }

  startWith(Idle, EmptyShoppingCart)

  when(Idle) {
    case Event(CreateOrderCommand(_), EmptyShoppingCart) =>
      println("Creating shopping cart...")
      goto(InShoppingCart) applying OrderCreatedEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order created!")
  }

  when(InShoppingCart) {
    case Event(AddItemToShoppingCartCommand(product), s@(_: NonEmptyShoppingCart | EmptyShoppingCart)) =>
      println("Adding: " + product + " to shopping cart: " + s)
      val availabilityCheckResultF = (productQuantityActor ? CheckProductAvailabilityCommand(product)).mapTo[ProductAvailabilityCheckedEvent]
      val stateF = availabilityCheckResultF.map { p =>
        if (p.isAvailable)
          stay applying ItemAddedToShoppingCartEvent(product) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "added item to shopping cart!")
        else
          stay applying ProductNotAvailableEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "product is not available!")
      }
      Await.result(stateF, Duration.Inf)
    case Event(ConfirmShoppingCartCommand(_), s: NonEmptyShoppingCart) =>
      println("Confirm shopping cart with products: " + s)
      goto(WaitingForChoosingDeliveryMethod) applying ConfirmedShoppingCartEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "confirm shopping cart!")
  }

  when(WaitingForChoosingDeliveryMethod) {
    case Event(ChooseDeliveryMethodCommand(deliveryMethod), s: NonEmptyShoppingCart) =>
      println("Delivery method: " + deliveryMethod)
      goto(WaitingForChoosingPaymentMethod) applying DeliveryMethodChosenEvent(s, deliveryMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "delivery method chosen!")
  }

  when(WaitingForChoosingPaymentMethod) {
    case Event(ChoosePaymentMethodCommand(paymentMethod), data: DataWithDeliveryMethod) =>
      println("Payment method: " + paymentMethod)
      goto(OrderReadyToCheckout) applying PaymentMethodChosenEvent(data, paymentMethod) replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "payment method chosen!")
  }

  when(OrderReadyToCheckout, timeout.duration) {
    case Event(CheckoutCommand(_), _) =>
      println("Processing order...")
      goto(OrderProcessed) applying CheckedOutEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "order processed!")
    case Event(StateTimeout, data: DataWithPaymentMethod) =>
      println("Timeout! Back to state InShoppingCart")
      data.shoppingCart.products.foreach { product =>
        productQuantityActor ! IncreaseQuantityOfProductCommand(product)
      }
      goto(InShoppingCart) applying OrderReadyTimeoutOccurredEvent
  }

  when(OrderProcessed) {
    case Event(OrderDisplayedEvent, _) =>
      println("Order processed!")
      deleteMessages(this.lastSequenceNr)
      stay
  }

  onTransition {
    case OrderReadyToCheckout -> OrderProcessed =>
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

  val shardName: String = "Order"
  val numberOfShards = 2

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case cmd: Command => (cmd.orderId.toString, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: Command => (math.abs(cmd.orderId.toString.hashCode()) % numberOfShards).toString
  }

  def props(displayOrderActor: ActorRef, productQuantityActor: ActorRef): Props = Props(classOf[OrderingProcessFSM], displayOrderActor, productQuantityActor)
}
