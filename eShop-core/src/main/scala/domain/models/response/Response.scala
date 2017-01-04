package domain.models.response

sealed trait Response

/**
  * Class used as a response in REST api methods (eShopRestApi module)
  */
case class FSMProcessInfoResponse(state: String,
                                  data: String,
                                  message: String) extends Response
