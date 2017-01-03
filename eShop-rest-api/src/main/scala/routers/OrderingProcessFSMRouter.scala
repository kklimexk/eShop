package routers

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import domain._
import domain.models._
import domain.models.response.OrderingProcessInfoResponse

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class OrderingProcessFSMRouter(orderingProcessFSM: ActorRef) extends JsonRouter {

  implicit val timeout = Timeout(2.seconds)

  override def route: Route = {
    createOrder ~
    addItemToBasket ~
    checkout ~
    chooseDeliveryMethod ~
    choosePaymentMethod ~
    processOrder
  }

  private def createOrder = {
    post {
      path("createOrder") {
        val responseF = (orderingProcessFSM ? CreateOrderCommand).mapTo[OrderingProcessInfoResponse]
        onComplete(responseF) {
          case Success(response) =>
            complete(response)
          case Failure(_) =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  private def addItemToBasket = {
    post {
      path("addItemToBasket") {
        entity(as[Product]) { product =>
          val responseF = (orderingProcessFSM ? AddItemToBasketCommand(product)).mapTo[OrderingProcessInfoResponse]
          onComplete(responseF) {
            case Success(response) =>
              complete(response)
            case Failure(_) =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def checkout = {
    post {
      path("checkout") {
        val responseF = (orderingProcessFSM ? CheckoutCommand).mapTo[OrderingProcessInfoResponse]
        onComplete(responseF) {
          case Success(response) =>
            complete(response)
          case Failure(_) =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

  private def chooseDeliveryMethod = {
    post {
      path("deliveryMethod") {
        entity(as[DeliveryMethodEntity]) { deliveryMethod =>
          val responseF = (orderingProcessFSM ? ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod.name))).mapTo[OrderingProcessInfoResponse]
          onComplete(responseF) {
            case Success(response) =>
              complete(response)
            case Failure(_) =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def choosePaymentMethod = {
    post {
      path("paymentMethod") {
        entity(as[PaymentMethodEntity]) { paymentMethod =>
          val responseF = (orderingProcessFSM ? ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod.name))).mapTo[OrderingProcessInfoResponse]
          onComplete(responseF) {
            case Success(response) =>
              complete(response)
            case Failure(_) =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  private def processOrder = {
    post {
      path("processOrder") {
        val responseF = (orderingProcessFSM ? ProcessOrderCommand).mapTo[OrderingProcessInfoResponse]
        onComplete(responseF) {
          case Success(response) =>
            complete(response)
          case Failure(_) =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }
  }

}
