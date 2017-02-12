package main

import webserver.WebServer
import webserver.config.SimpleConfig

object App {
  def main(args: Array[String]): Unit = {
    val webServer = new WebServer() with SimpleConfig
    webServer.run
  }
}
