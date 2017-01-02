package routers

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import domain._
import domain.models._

class OrderingProcessFSMRouter(orderingProcessFSM: ActorRef) extends JsonRouter {

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
        orderingProcessFSM ! CreateOrderCommand
        complete("order created or already created!")
      }
    }
  }

  private def addItemToBasket = {
    post {
      path("addItemToBasket") {
        entity(as[Product]) { product =>
          orderingProcessFSM ! AddItemToBasketCommand(product)
          complete("added item to basket!")
        }
      }
    }
  }

  private def checkout = {
    post {
      path("checkout") {
        orderingProcessFSM ! CheckoutCommand
        complete("checkout!")
      }
    }
  }

  private def chooseDeliveryMethod = {
    post {
      path("deliveryMethod") {
        entity(as[DeliveryMethodEntity]) { deliveryMethod =>
          orderingProcessFSM ! ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod.name))
          complete("delivery method chosen!")
        }
      }
    }
  }

  private def choosePaymentMethod = {
    post {
      path("paymentMethod") {
        entity(as[PaymentMethodEntity]) { paymentMethod =>
          orderingProcessFSM ! ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod.name))
          complete("payment method chosen!")
        }
      }
    }
  }

  private def processOrder = {
    post {
      path("processOrder") {
        orderingProcessFSM ! ProcessOrderCommand
        complete("order processed!")
      }
    }
  }

}
