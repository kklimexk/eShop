package main

import actors.DisplayOrderActor
import domain.{ProcessOrderCommand, _}
import fsm.OrderingProcessFSM
import models.{DeliveryMethod, PaymentMethod}
import shared.Global.Implicits._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Main {
  def main(args: Array[String]): Unit = {

    val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
    val orderingProcessFSM = system.actorOf(OrderingProcessFSM.props(displayOrderActor), "Order1")

    orderingProcessFSM ! CreateOrderCommand

    orderingProcessFSM ! AddItemToBasketCommand(models.Product(1, "iPhone 5s"))
    orderingProcessFSM ! AddItemToBasketCommand(models.Product(2, "The Witcher 3"))

    orderingProcessFSM ! CheckoutCommand

    orderingProcessFSM ! ChooseDeliveryMethodCommand(deliveryMethod = DeliveryMethod.Courier)
    orderingProcessFSM ! ChoosePaymentMethodCommand(paymentMethod = PaymentMethod.CreditCard)

    orderingProcessFSM ! ProcessOrderCommand

    Await.ready(system.whenTerminated, Duration.Inf)
  }
}
