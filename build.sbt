lazy val commonSettings = Seq(
  name := "eShop",
  version := "1.0",
  scalaVersion := "2.11.8"
)

lazy val akkaVersion = "2.4.16"
lazy val akkaHttpVersion = "10.0.1"
lazy val scalaTestVersion = "3.0.1"

lazy val root = (project in file(".")).
  settings(commonSettings: _*).
  settings(
    name := "eShop"
  ).aggregate(eShopCore, eShopRestApi, eShopShared).dependsOn(eShopCore, eShopShared)

lazy val eShopCore = (project in file("eShop-core")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-core",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion,
      "org.scalactic" %% "scalactic" % scalaTestVersion,
      "org.scalatest" %% "scalatest" % scalaTestVersion % "test"
    )
  )

lazy val eShopRestApi = (project in file("eShop-rest-api")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-rest-api",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
    )
  ).dependsOn(eShopCore, eShopShared)

lazy val eShopShared = (project in file("eShop-shared")).
  settings(commonSettings: _*).
  settings(
    name := "eShop-shared",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-actor" % akkaVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion
    )
  )
