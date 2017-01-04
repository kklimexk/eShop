package routers

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import akka.pattern.ask

import domain._
import domain.models._

import utils.ResponseUtil

import scala.concurrent.duration._

class OrderingProcessFSMRouter(orderingProcessFSM: ActorRef) extends JsonRouter with ResponseUtil {

  implicit val timeout = Timeout(10.seconds)

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
        response(orderingProcessFSM ? CreateOrderCommand)
      }
    }
  }

  private def addItemToBasket = {
    post {
      path("addItemToBasket") {
        entity(as[Product]) { product =>
          response(orderingProcessFSM ? AddItemToBasketCommand(product))
        }
      }
    }
  }

  private def checkout = {
    post {
      path("checkout") {
        response(orderingProcessFSM ? CheckoutCommand)
      }
    }
  }

  private def chooseDeliveryMethod = {
    post {
      path("deliveryMethod") {
        entity(as[DeliveryMethodEntity]) { deliveryMethod =>
          response(orderingProcessFSM ? ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod.name)))
        }
      }
    }
  }

  private def choosePaymentMethod = {
    post {
      path("paymentMethod") {
        entity(as[PaymentMethodEntity]) { paymentMethod =>
          response(orderingProcessFSM ? ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod.name)))
        }
      }
    }
  }

  private def processOrder = {
    post {
      path("processOrder") {
        response(orderingProcessFSM ? ProcessOrderCommand)
      }
    }
  }

}
