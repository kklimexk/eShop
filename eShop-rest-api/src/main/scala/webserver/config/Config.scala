package webserver.config

import routers.MainRouter
import shared.AkkaSettings

trait Config extends AkkaSettings {
  val mainRouter: MainRouter
}
