lazy val commonSettings = Seq(
  name := "eShop",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val akkaVersion = "2.4.16"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion
    )
  )
