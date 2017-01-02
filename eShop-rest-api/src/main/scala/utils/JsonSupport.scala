package utils

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import domain.models.{DeliveryMethodEntity, PaymentMethodEntity, Product}
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Product)
  implicit val deliveryMethodFormat = jsonFormat1(DeliveryMethodEntity)
  implicit val paymentMethodFormat = jsonFormat1(PaymentMethodEntity)
}
