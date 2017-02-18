package main

import webserver.WebServer
import webserver.config.sharded.{ShardedConfig, WebServerShardedConfig}

import shared.AkkaShardedSettings._

object ShardedApp extends ShardedConfig {
  def main(args: Array[String]): Unit = {

    //set property -Dakka.remote.netty.tcp.port
    val port = System.getProperty("akka.remote.netty.tcp.port")

    if (port == "0") {
      val webServer = new WebServer() with WebServerShardedConfig
      webServer.run
    }
  }
}
