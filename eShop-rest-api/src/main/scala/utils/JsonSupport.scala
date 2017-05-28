package utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport

import domain.models.response.{FSMProcessInfoResponse, SimpleResponse}
import domain.models.{DeliveryMethodEntity, PaymentMethodEntity}

import shared.models.ProductOrderItem
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val productOrderItemFormat = jsonFormat2(ProductOrderItem)
  implicit val deliveryMethodFormat = jsonFormat1(DeliveryMethodEntity)
  implicit val paymentMethodFormat = jsonFormat1(PaymentMethodEntity)
  implicit val fsmProcessInfoResponseFormat = jsonFormat3(FSMProcessInfoResponse)
  implicit val simpleResponseFormat = jsonFormat1(SimpleResponse)
}
