package routers

import akka.actor.ActorRef
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import domain._
import domain.models.{DeliveryMethod, PaymentMethod, Product}

class OrderingProcessFSMRouter(orderingProcessFSM: ActorRef) extends Router {

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
      path("addItemToBasket" / "id" / LongNumber / "name" / """\w+""".r) { (id, name) =>
        orderingProcessFSM ! AddItemToBasketCommand(Product(id, name))
        complete("added item to basket!")
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
      path("deliveryMethod" / """\w+""".r) { deliveryMethod =>
        orderingProcessFSM ! ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.withName(deliveryMethod))
        complete("delivery method chosen!")
      }
    }
  }

  private def choosePaymentMethod = {
    post {
      path("paymentMethod" / """\w+""".r) { paymentMethod =>
        orderingProcessFSM ! ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.withName(paymentMethod))
        complete("payment method chosen!")
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
