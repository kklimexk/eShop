package routers

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask

import domain._
import domain.models._
import domain.models.response.{FSMProcessInfoResponse, SimpleResponse}

import persistence_fsm.OrderingProcessFSM

import utils.ResponseUtil

import scala.concurrent.duration._
import scala.concurrent.Future
import scala.util.{Failure, Success}

class OrderingProcessFSMRouter(displayOrderActor: ActorRef,
                               productQuantityActor: ActorRef)
                              (orderingProcessRegion: Option[ActorRef] = None)
                              (implicit system: ActorSystem) extends JsonRouter with ResponseUtil {

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
        def orElseResponse = (system.actorOf(OrderingProcessFSM.props(displayOrderActor, productQuantityActor), "order" + orderId) ? CreateOrderCommand(orderId)).mapTo[FSMProcessInfoResponse]

        val responseF = extendedResponse[FSMProcessInfoResponse, FSMProcessInfoResponse](actorPath(orderId), CreateOrderCommand(orderId))(orElseResponse)

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
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse](actorPath(orderId), AddItemToShoppingCartCommand(product))(orderingProcessFSMFailureResponse)
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
        val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse](actorPath(orderId), CheckoutCommand(orderId))(orderingProcessFSMFailureResponse)
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
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse](actorPath(orderId), ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod.name)))(orderingProcessFSMFailureResponse)
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
          val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse](actorPath(orderId), ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod.name)))(orderingProcessFSMFailureResponse)
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
        val responseF = extendedResponse[FSMProcessInfoResponse, SimpleResponse](actorPath(orderId), ProcessOrderCommand(orderId))(orderingProcessFSMFailureResponse)
        onComplete(responseF) {
          case Success(route) => route
          case Failure(ex) => complete(ex)
        }
      }
    }
  }

  private def actorPath(orderId: Long): String = {
    val res = orderingProcessRegion match {
      case Some(orderingProcessR) => orderingProcessR.path.toString
      case _ => "/user/order" + orderId
    }
    res
  }

}
