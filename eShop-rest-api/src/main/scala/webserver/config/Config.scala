package webserver.config

import routers.MainRouter

trait Config {
  val mainRouter: MainRouter
}
