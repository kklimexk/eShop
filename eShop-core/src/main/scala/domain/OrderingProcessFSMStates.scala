package domain

import akka.persistence.fsm.PersistentFSM.FSMState

sealed trait OrderingProcessFSMState extends FSMState

case object Idle extends OrderingProcessFSMState {
  override def identifier: String = "Idle"
}

case object InBasket extends OrderingProcessFSMState {
  override def identifier: String = "InBasket"
}

case object WaitingForChoosingDeliveryMethod extends OrderingProcessFSMState {
  override def identifier: String = "WaitingForChoosingDeliveryMethod"
}

case object WaitingForChoosingPaymentMethod extends OrderingProcessFSMState {
  override def identifier: String = "WaitingForChoosingPaymentMethod"
}

case object OrderReadyToProcess extends OrderingProcessFSMState {
  override def identifier: String = "OrderReadyToProcess"
}

case object OrderProcessed extends OrderingProcessFSMState {
  override def identifier: String = "OrderProcessed"
}
