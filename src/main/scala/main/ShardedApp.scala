package main

import webserver.WebServer
import webserver.config.ShardedConfig

object ShardedApp {
  def main(args: Array[String]): Unit = {
    val webServer = new WebServer() with ShardedConfig
    webServer.run
  }
}
