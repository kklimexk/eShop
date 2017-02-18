package main

import webserver.WebServer
import webserver.config.simple.WebServerSimpleConfig

import shared.AkkaSimpleSettings._

object App {
  def main(args: Array[String]): Unit = {
    val webServer = new WebServer() with WebServerSimpleConfig
    webServer.run
  }
}
