package domain

import akka.persistence.fsm.PersistentFSM.FSMState

sealed trait OrderingProcessFSMState extends FSMState

case object Idle extends OrderingProcessFSMState {
  override def identifier: String = "Idle"
}

case object InShoppingCart extends OrderingProcessFSMState {
  override def identifier: String = "InShoppingCart"
}

case object WaitingForChoosingDeliveryMethod extends OrderingProcessFSMState {
  override def identifier: String = "WaitingForChoosingDeliveryMethod"
}

case object WaitingForChoosingPaymentMethod extends OrderingProcessFSMState {
  override def identifier: String = "WaitingForChoosingPaymentMethod"
}

case object OrderReadyToCheckout extends OrderingProcessFSMState {
  override def identifier: String = "OrderReadyToCheckout"
}

case object OrderProcessed extends OrderingProcessFSMState {
  override def identifier: String = "OrderProcessed"
}
