package domain.models.response

/**
  * Class used as a response in REST api methods (eShopRestApi module)
  */
case class OrderingProcessInfoResponse(state: String,
                                       data: String,
                                       message: String)
