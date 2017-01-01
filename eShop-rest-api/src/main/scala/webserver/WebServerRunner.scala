package webserver

object WebServerRunner {
  def main(args: Array[String]): Unit = {
    val webServer = new WebServer()
    webServer.run
  }
}
