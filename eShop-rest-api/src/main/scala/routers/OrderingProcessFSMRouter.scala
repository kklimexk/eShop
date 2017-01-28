package routers

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask

import domain._
import domain.models._
import domain.models.response.{FSMProcessInfoResponse, SimpleResponse}

import persistence_fsm.OrderingProcessFSM
import shared.Global.Implicits.system
import utils.ResponseUtil

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class OrderingProcessFSMRouter(displayOrderActor: ActorRef,
                               productQuantityActor: ActorRef) extends JsonRouter with ResponseUtil {

  implicit val timeout = Timeout(5.seconds)
  val orderingProcessFSMFailureResponse = Future.successful(SimpleResponse("Order does not exist!"))

  override def route: Route = {
    createOrder ~
    pathPrefix("orderId" / LongNumber) { implicit orderId =>
      addItemToShoppingCart ~
      checkout ~
      chooseDeliveryMethod ~
      choosePaymentMethod ~
      processOrder
    }
  }

  private def createOrder = {
    post {
      path("createOrder" / LongNumber) { orderId =>
        def orElseResponse = (system.actorOf(OrderingProcessFSM.props(displayOrderActor, productQuantityActor), "order" + orderId) ? CreateOrderCommand).mapTo[FSMProcessInfoResponse]

        val responseF = extendedResponse[FSMProcessInfoResponse, FSMProcessInfoResponse]("/user/order" + orderId, CreateOrderCommand)(orElseResponse)
        onComplete(responseF) {
          case Success(route) => route
          case Failure(ex) => complete(ex)
        }
      }
    }
  }

  private def addItemToShoppingCart(implicit orderId: Long) = {
    post {
      path("addItemToShoppingCart") {
        entity(as[Product]) { product =>
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse]("/user/order" + orderId, AddItemToShoppingCartCommand(product))(orderingProcessFSMFailureResponse)
          onComplete(responseF) {
            case Success(route) => route
            case Failure(ex) => complete(ex)
          }
        }
      }
    }
  }

  private def checkout(implicit orderId: Long) = {
    post {
      path("checkout") {
        val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse]("/user/order" + orderId, CheckoutCommand)(orderingProcessFSMFailureResponse)
        onComplete(responseF) {
          case Success(route) => route
          case Failure(ex) => complete(ex)
        }
      }
    }
  }

  private def chooseDeliveryMethod(implicit orderId: Long) = {
    post {
      path("deliveryMethod") {
        entity(as[DeliveryMethodEntity]) { deliveryMethod =>
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse]("/user/order" + orderId, ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod.name)))(orderingProcessFSMFailureResponse)
          onComplete(responseF) {
            case Success(route) => route
            case Failure(ex) => complete(ex)
          }
        }
      }
    }
  }

  private def choosePaymentMethod(implicit orderId: Long) = {
    post {
      path("paymentMethod") {
        entity(as[PaymentMethodEntity]) { paymentMethod =>
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse]("/user/order" + orderId, ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod.name)))(orderingProcessFSMFailureResponse)
          onComplete(responseF) {
            case Success(route) => route
            case Failure(ex) => complete(ex)
          }
        }
      }
    }
  }

  private def processOrder(implicit orderId: Long) = {
    post {
      path("processOrder") {
        val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse]("/user/order" + orderId, ProcessOrderCommand)(orderingProcessFSMFailureResponse)
        onComplete(responseF) {
          case Success(route) => route
          case Failure(ex) => complete(ex)
        }
      }
    }
  }

}
