package actors

import actors.DisplayOrderActor.{DisplayOrderCommand, OrderDisplayedEvent}
import akka.actor.{Actor, Props}
import domain.DataOrder

class DisplayOrderActor extends Actor {
  override def receive = {
    case DisplayOrderCommand(order) =>
      println("Order: " + order + " processed")
      sender ! OrderDisplayedEvent
  }
}

object DisplayOrderActor {
  def props: Props = Props[DisplayOrderActor]

  sealed trait DisplayOrderActorCommand
  case class DisplayOrderCommand(order: DataOrder) extends DisplayOrderActorCommand

  sealed trait DisplayOrderActorEvent
  case object OrderDisplayedEvent extends DisplayOrderActorEvent
}
