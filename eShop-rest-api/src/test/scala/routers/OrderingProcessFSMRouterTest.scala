package routers

import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import org.scalatest.{FunSuiteLike, Matchers}
import actors.{DisplayOrderActor, ProductQuantityActor}

import domain.models._
import domain._
import domain.models.response.FSMProcessInfoResponse

import scala.concurrent.duration._
import utils.JsonSupport

class OrderingProcessFSMRouterTest extends FunSuiteLike
  with Matchers with ScalatestRouteTest with JsonSupport {

  private implicit val routeTestTimeout = RouteTestTimeout(5.second)

  private val product1: Product = Product(1, "iPhone")
  private val product2: Product = Product(3, "Computer")

  private val displayOrderActor = system.actorOf(DisplayOrderActor.props, "DisplayOrderActor")
  private val productQuantityActor = system.actorOf(ProductQuantityActor.props, "ProductQuantityActor")
  private val orderingProcessFSMRouter = new OrderingProcessFSMRouter(displayOrderActor, productQuantityActor)

  private val route = orderingProcessFSMRouter.route

  test("Ordering process FSM router") {
    Post("/createOrder/1") ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(Idle.toString, EmptyShoppingCart.toString, "order created!")
    }
    Post("/orderId/1/addItemToShoppingCart", product1) ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(InShoppingCart.toString, EmptyShoppingCart.toString, "added item to shopping cart!")
    }
    Post("/orderId/1/addItemToShoppingCart", product2) ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1)).toString, "added item to shopping cart!")
    }
    Post("/orderId/1/addItemToShoppingCart", product2) ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "product is not available!")
    }
    Post("/orderId/1/checkout") ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(InShoppingCart.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "checkout!")
    }
    Post("/orderId/1/deliveryMethod", DeliveryMethodEntity("Courier")) ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(WaitingForChoosingDeliveryMethod.toString, NonEmptyShoppingCart(Seq(product1, product2)).toString, "delivery method chosen!")
    }
    Post("/orderId/1/paymentMethod", PaymentMethodEntity("CreditCard")) ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(WaitingForChoosingPaymentMethod.toString, DataWithDeliveryMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier).toString, "payment method chosen!")
    }
    Post("/orderId/1/processOrder") ~> route ~> check {
      responseAs[FSMProcessInfoResponse] shouldEqual FSMProcessInfoResponse(OrderReadyToProcess.toString, DataWithPaymentMethod(NonEmptyShoppingCart(Seq(product1, product2)), DeliveryMethod.Courier, PaymentMethod.CreditCard).toString, "order processed!")
    }
  }
}
