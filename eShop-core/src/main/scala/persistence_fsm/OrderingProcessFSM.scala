package persistence_fsm

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}
import actors.ProductQuantityActor.{CheckProductAvailabilityCommand, IncreaseQuantityOfProductCommand, ProductAvailabilityCheckedEvent}

import akka.actor.{ActorRef, Props, Stash}
import akka.cluster.sharding.ShardRegion
import akka.persistence.DeleteMessagesSuccess
import akka.persistence.fsm.PersistentFSM
import akka.pattern.ask
import akka.util.Timeout

import domain.{ConfirmedShoppingCartEvent, DeliveryMethodChosenEvent, _}
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._
import shared.DefaultThreadPool._

import scala.reflect._

class OrderingProcessFSM(displayOrderActor: ActorRef,
                         productQuantityActor: ActorRef) extends PersistentFSM[OrderingProcessFSMState, OrderingProcessFSMData, OrderingProcessFSMEvent] with Stash {

  override def domainEventClassTag: ClassTag[OrderingProcessFSMEvent] = classTag[OrderingProcessFSMEvent]
  override def persistenceId: String = "OrderingProcessFSM" + self.path

  implicit val timeout = Timeout(10.seconds)

  override def applyEvent(domainEvent: OrderingProcessFSMEvent, currentData: OrderingProcessFSMData): OrderingProcessFSMData = {
    domainEvent match {
      case OrderCreatedEvent => currentData.empty()
      case ItemAddedToShoppingCartEvent(product) => currentData.addItem(product)
      case ProductNotAvailableEvent => currentData
      case AddingItemToShoppingCartEvent => currentData
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
    case Event(addItem@AddItemToShoppingCartCommand(product), s@(_: NonEmptyShoppingCart | EmptyShoppingCart)) =>
      println("Adding: " + product + " to shopping cart: " + s)
      val availabilityCheckResultF = (productQuantityActor ? CheckProductAvailabilityCommand(product)).mapTo[ProductAvailabilityCheckedEvent]
      val stateF = availabilityCheckResultF.flatMap { p =>
        val isAvailableF = p.isAvailable
        isAvailableF map { isAvailable =>
          if (isAvailable)
            self forward addItem
          else
            self forward ProductNotAvailableCommand
        }
      }
      stateF.onComplete(_ => ())

      goto(InShoppingCartPartial) applying AddingItemToShoppingCartEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "adding item to shopping cart!")
    case Event(ConfirmShoppingCartCommand(_), s: NonEmptyShoppingCart) =>
      println("Confirm shopping cart with products: " + s)
      goto(WaitingForChoosingDeliveryMethod) applying ConfirmedShoppingCartEvent replying FSMProcessInfoResponse(stateName.toString, stateData.toString, "confirm shopping cart!")
  }

  when(InShoppingCartPartial) {
    case Event(AddItemToShoppingCartCommand(product), _) =>
      println("Added item: " + product + " to shopping cart!")
      goto(InShoppingCart) applying ItemAddedToShoppingCartEvent(product)
    case Event(ProductNotAvailableCommand, _) =>
      println("Product is not available!")
      goto(InShoppingCart) applying ProductNotAvailableEvent
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
    case InShoppingCartPartial -> InShoppingCart => unstashAll()
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
    case Event(AddItemToShoppingCartCommand(_) | ProductNotAvailableCommand, _) =>
      stash()
      stay()
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
    case cmd: OrderCommand => (cmd.orderId.toString, cmd)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case cmd: OrderCommand => (math.abs(cmd.orderId.toString.hashCode()) % numberOfShards).toString
  }

  def props(displayOrderActor: ActorRef, productQuantityActor: ActorRef): Props = Props(classOf[OrderingProcessFSM], displayOrderActor, productQuantityActor)
}
